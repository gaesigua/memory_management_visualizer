package com.mmv.memory_management_visualizer.service;

import com.mmv.memory_management_visualizer.model.MemoryState;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MemoryService {
    private MemoryState currentState;
    private final int PAGE_SIZE = 64; // Default standard page size in KB

    public MemoryService() {
        initializeMemory("best-fit", 1024);
    }

    public MemoryState initializeMemory(String strategy, int totalSize) {
        this.currentState = new MemoryState(strategy, totalSize);
        List<MemoryState.MemoryBlock> blocks = new ArrayList<>();
        currentState.setPageTables(new HashMap<>());
        currentState.setSegmentTables(new HashMap<>());

        if ("fixed".equalsIgnoreCase(strategy)) {
            int numPartitions = 4;
            int partitionSize = totalSize / numPartitions;
            for (int i = 0; i < numPartitions; i++) {
                blocks.add(new MemoryState.MemoryBlock("fixed-" + i, i * partitionSize, partitionSize, false, null));
            }
        } else if ("paging".equalsIgnoreCase(strategy)) {
            int numFrames = totalSize / PAGE_SIZE;
            for (int i = 0; i < numFrames; i++) {
                blocks.add(new MemoryState.MemoryBlock("frame-" + i, i * PAGE_SIZE, PAGE_SIZE, false, null));
            }
        } else {
            // Default uniform block for dynamic partitioning and segmentation tracking lines
            blocks.add(new MemoryState.MemoryBlock("block-0", 0, totalSize, false, null));
        }

        this.currentState.setBlocks(blocks);
        recomputeMetrics();
        return currentState;
    }

    public MemoryState allocateProcess(String processId, int requestedSize) {
        if (currentState == null) initializeMemory("best-fit", 1024);

        switch (currentState.getStrategy().toLowerCase()) {
            case "fixed":
                return allocateFixed(processId, requestedSize);
            case "paging":
                return allocatePaging(processId, requestedSize);
            case "segmentation":
                return allocateSegmentation(processId, requestedSize);
            default:
                return allocateBestFit(processId, requestedSize);
        }
    }

    private MemoryState allocateFixed(String processId, int requestedSize) {
        MemoryState.MemoryBlock bestPartition = null;
        for (MemoryState.MemoryBlock block : currentState.getBlocks()) {
            if (!block.isAllocated() && block.getSize() >= requestedSize) {
                if (bestPartition == null || block.getSize() < bestPartition.getSize()) {
                    bestPartition = block;
                }
            }
        }
        if (bestPartition != null) {
            bestPartition.setAllocated(true);
            bestPartition.setProcessId(processId);
            // Storing the actual process requirement inside the node block map metadata
            bestPartition.setId(bestPartition.getId() + "_used_" + requestedSize);
            recomputeMetrics();
        }
        return currentState;
    }

    private MemoryState allocateBestFit(String processId, int requestedSize) {
        MemoryState.MemoryBlock bestBlock = null;
        List<MemoryState.MemoryBlock> blocks = currentState.getBlocks();

        for (MemoryState.MemoryBlock block : blocks) {
            if (!block.isAllocated() && block.getSize() >= requestedSize) {
                if (bestBlock == null || block.getSize() < bestBlock.getSize()) {
                    bestBlock = block;
                }
            }
        }

        if (bestBlock != null) {
            int originalSize = bestBlock.getSize();
            if (originalSize > requestedSize) {
                bestBlock.setSize(requestedSize);
                bestBlock.setAllocated(true);
                bestBlock.setProcessId(processId);

                int remainderSize = originalSize - requestedSize;
                int remainderStart = bestBlock.getStartAddress() + requestedSize;
                MemoryState.MemoryBlock remainder = new MemoryState.MemoryBlock(
                    "block-" + remainderStart, remainderStart, remainderSize, false, null
                );

                int idx = blocks.indexOf(bestBlock);
                if (idx >= 0) blocks.add(idx + 1, remainder);
            } else {
                bestBlock.setAllocated(true);
                bestBlock.setProcessId(processId);
            }
            recomputeMetrics();
        }
        return currentState;
    }

    private MemoryState allocatePaging(String processId, int requestedSize) {
        int pagesNeeded = (int) Math.ceil((double) requestedSize / PAGE_SIZE);
        List<MemoryState.MemoryBlock> blocks = currentState.getBlocks();
        List<Integer> freeFrameIndices = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            if (!blocks.get(i).isAllocated()) freeFrameIndices.add(i);
        }

        if (freeFrameIndices.size() < pagesNeeded) return currentState; // Out of memory

        List<Integer> assignedFrames = new ArrayList<>();
        for (int i = 0; i < pagesNeeded; i++) {
            int frameIdx = freeFrameIndices.get(i);
            MemoryState.MemoryBlock frame = blocks.get(frameIdx);
            frame.setAllocated(true);
            frame.setProcessId(processId);
            assignedFrames.add(frameIdx);
        }

        currentState.getPageTables().put(processId, assignedFrames);
        // Track the leftover tail internal waste size directly via metadata string tags
        int totalAllocatedSpace = pagesNeeded * PAGE_SIZE;
        int waste = totalAllocatedSpace - requestedSize;
        if (waste > 0 && !assignedFrames.isEmpty()) {
            MemoryState.MemoryBlock lastFrame = blocks.get(assignedFrames.get(assignedFrames.size() - 1));
            lastFrame.setId(lastFrame.getId() + "_waste_" + waste);
        }

        recomputeMetrics();
        return currentState;
    }

    private MemoryState allocateSegmentation(String processId, int requestedSize) {
        // Break incoming request into standard logical blocks: Code (40%), Data (40%), Stack (20%)
        int codeSize = (int) (requestedSize * 0.4);
        int dataSize = (int) (requestedSize * 0.4);
        int stackSize = requestedSize - (codeSize + dataSize);

        String[] segNames = {"Code", "Data", "Stack"};
        int[] segSizes = {codeSize, dataSize, stackSize};
        List<MemoryState.SegmentEntry> entries = new ArrayList<>();

        // Use sequential Best-Fit execution passes to isolate placement spans
        for (int i = 0; i < 3; i++) {
            int targetSize = segSizes[i];
            MemoryState.MemoryBlock bestHole = null;
            for (MemoryState.MemoryBlock b : currentState.getBlocks()) {
                if (!b.isAllocated() && b.getSize() >= targetSize) {
                    if (bestHole == null || b.getSize() < bestHole.getSize()) bestHole = b;
                }
            }

            if (bestHole == null) return currentState; // Single segment allocation failure cancels sequence

            int originalSize = bestHole.getSize();
            bestHole.setSize(targetSize);
            bestHole.setAllocated(true);
            bestHole.setProcessId(processId + " [" + segNames[i] + "]");

            entries.add(new MemoryState.SegmentEntry(segNames[i], bestHole.getStartAddress(), targetSize));

            if (originalSize > targetSize) {
                int remainderSize = originalSize - targetSize;
                int remainderStart = bestHole.getStartAddress() + targetSize;
                MemoryState.MemoryBlock remainder = new MemoryState.MemoryBlock(
                    "block-" + remainderStart, remainderStart, remainderSize, false, null
                );
                int idx = currentState.getBlocks().indexOf(bestHole);
                currentState.getBlocks().add(idx + 1, remainder);
            }
        }

        currentState.getSegmentTables().put(processId, entries);
        recomputeMetrics();
        return currentState;
    }

    public MemoryState deallocateProcess(String processId) {
        if (currentState == null) return null;
        List<MemoryState.MemoryBlock> blocks = currentState.getBlocks();

        // Clear mappings from non-contiguous index tables
        currentState.getPageTables().remove(processId);
        currentState.getSegmentTables().remove(processId);

        for (MemoryState.MemoryBlock b : blocks) {
            if (b.isAllocated() && (processId.equals(b.getProcessId()) || (b.getProcessId() != null && b.getProcessId().startsWith(processId)))) {
                b.setAllocated(false);
                b.setProcessId(null);
                // Clean structural tracking meta flags
                if (b.getId().contains("_used_")) b.setId(b.getId().split("_used_")[0]);
                if (b.getId().contains("_waste_")) b.setId(b.getId().split("_waste_")[0]);
            }
        }

        // Run coalescing optimization sweep for contiguous dynamic block management configurations
        String strategy = currentState.getStrategy();
        if (!"fixed".equalsIgnoreCase(strategy) && !"paging".equalsIgnoreCase(strategy)) {
            for (int i = 0; i < blocks.size() - 1; ) {
                MemoryState.MemoryBlock curr = blocks.get(i);
                MemoryState.MemoryBlock next = blocks.get(i + 1);
                if (!curr.isAllocated() && !next.isAllocated()) {
                    curr.setSize(curr.getSize() + next.getSize());
                    blocks.remove(i + 1);
                } else {
                    i++;
                }
            }
        }

        recomputeMetrics();
        return currentState;
    }

    private void recomputeMetrics() {
        int used = 0;
        int internal = 0;
        int external = 0;
        String strategy = currentState.getStrategy();

        for (MemoryState.MemoryBlock b : currentState.getBlocks()) {
            if (b.isAllocated()) {
                if ("fixed".equalsIgnoreCase(strategy)) {
                    int originalCapacity = 256; // Dynamic structural check fallback
                    if (b.getId().contains("_used_")) {
                        int actualAlloc = Integer.parseInt(b.getId().split("_used_")[1]);
                        used += actualAlloc;
                        internal += (originalCapacity - actualAlloc);
                    } else {
                        used += b.getSize();
                    }
                } else if ("paging".equalsIgnoreCase(strategy)) {
                    if (b.getId().contains("_waste_")) {
                        int wasteAmount = Integer.parseInt(b.getId().split("_waste_")[1]);
                        internal += wasteAmount;
                        used += (b.getSize() - wasteAmount);
                    } else {
                        used += b.getSize();
                    }
                } else {
                    used += b.getSize();
                }
            } else {
                if (!"fixed".equalsIgnoreCase(strategy) && !"paging".equalsIgnoreCase(strategy)) {
                    external += b.getSize();
                }
            }
        }

        if ("fixed".equalsIgnoreCase(strategy) || "paging".equalsIgnoreCase(strategy)) {
            int freeSpace = 0;
            for (MemoryState.MemoryBlock b : currentState.getBlocks()) {
                if (!b.isAllocated()) freeSpace += b.getSize();
            }
            currentState.setUsedSize(used);
            currentState.setInternalFragmentation(internal);
            currentState.setExternalFragmentation(freeSpace); // Representing leftover aggregate block boundaries
        } else {
            currentState.setUsedSize(used);
            currentState.setInternalFragmentation(0);
            currentState.setExternalFragmentation(external);
        }
    }

    public MemoryState getCurrentState() { return currentState; }
}