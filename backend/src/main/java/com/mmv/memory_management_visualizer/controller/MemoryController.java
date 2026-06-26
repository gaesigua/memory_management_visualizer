package com.mmv.memory_management_visualizer.controller;

import com.mmv.memory_management_visualizer.model.MemoryState;
import com.mmv.memory_management_visualizer.service.MemoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/memory")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:4173"
}, originPatterns = {
    "https://*.vercel.app",
    "https://*.onrender.com"
})
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    /**
     * Initializes a fresh memory system layout based on the chosen strategy.
     * @param strategy "fixed" or "best-fit"
     * @param totalSize Total allocation RAM size (e.g., 1024)
     */
    @PostMapping("/init")
    public MemoryState initializeSystem(@RequestParam String strategy, @RequestParam int totalSize) {
        return memoryService.initializeMemory(strategy, totalSize);
    }

    /**
     * Allocates an incoming process into memory using active strategy rules.
     */
    @PostMapping("/allocate")
    public MemoryState allocate(@RequestParam String processId, @RequestParam int size) {
        return memoryService.allocateProcess(processId, size);
    }

    /**
     * Removes a process block from memory and runs a coalescing cleanup if dynamic.
     */
    @PostMapping("/deallocate")
    public MemoryState deallocate(@RequestParam String processId) {
        return memoryService.deallocateProcess(processId);
    }

    /**
     * Retrieves the current structural layout and fragmentation metrics.
     */
    @GetMapping("/state")
    public MemoryState state() {
        return memoryService.getCurrentState();
    }
}