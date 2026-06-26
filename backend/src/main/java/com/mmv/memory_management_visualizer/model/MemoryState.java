package com.mmv.memory_management_visualizer.model;

import java.util.List;
import java.util.Map;

public class MemoryState {
    private String strategy;
    private int totalSize;
    private int usedSize;
    private int internalFragmentation;
    private int externalFragmentation;
    private List<MemoryBlock> blocks;
    
    // Non-contiguous tracking tables
    private Map<String, List<Integer>> pageTables; 
    private Map<String, List<SegmentEntry>> segmentTables;

    public MemoryState(String strategy, int totalSize) {
        this.strategy = strategy;
        this.totalSize = totalSize;
    }

    // --- Core Getters and Setters ---
    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public int getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(int usedSize) {
        this.usedSize = usedSize;
    }

    public int getInternalFragmentation() {
        return internalFragmentation;
    }

    public void setInternalFragmentation(int internalFragmentation) {
        this.internalFragmentation = internalFragmentation;
    }

    public int getExternalFragmentation() {
        return externalFragmentation;
    }

    public void setExternalFragmentation(int externalFragmentation) {
        this.externalFragmentation = externalFragmentation;
    }

    public List<MemoryBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<MemoryBlock> blocks) {
        this.blocks = blocks;
    }

    public Map<String, List<Integer>> getPageTables() {
        return pageTables;
    }

    public void setPageTables(Map<String, List<Integer>> pageTables) {
        this.pageTables = pageTables;
    }

    public Map<String, List<SegmentEntry>> getSegmentTables() {
        return segmentTables;
    }

    public void setSegmentTables(Map<String, List<SegmentEntry>> segmentTables) {
        this.segmentTables = segmentTables;
    }

    // --- Inner Classes for Memory Structures ---
    public static class MemoryBlock {
        private String id;
        private int startAddress;
        private int size;
        private boolean isAllocated;
        private String processId;

        public MemoryBlock(String id, int startAddress, int size, boolean isAllocated, String processId) {
            this.id = id;
            this.startAddress = startAddress;
            this.size = size;
            this.isAllocated = isAllocated;
            this.processId = processId;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public int getStartAddress() { return startAddress; }
        public void setStartAddress(int startAddress) { this.startAddress = startAddress; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public boolean isAllocated() { return isAllocated; }
        public void setAllocated(boolean allocated) { this.isAllocated = allocated; }

        public String getProcessId() { return processId; }
        public void setProcessId(String processId) { this.processId = processId; }
    }

    public static class SegmentEntry {
        private String name;
        private int baseAddress;
        private int limit;

        public SegmentEntry(String name, int baseAddress, int limit) {
            this.name = name;
            this.baseAddress = baseAddress;
            this.limit = limit;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getBaseAddress() { return baseAddress; }
        public void setBaseAddress(int baseAddress) { this.baseAddress = baseAddress; }

        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
    }
}