export interface MemoryBlock {
  id: string;
  startAddress: number;
  size: number;
  isAllocated: boolean;
  processId: string | null;
}

export interface SegmentEntry {
  name: string;
  baseAddress: number;
  limit: number;
}

export interface MemorySnapshot {
  strategy: string;
  totalSize: number;
  usedSize: number;
  internalFragmentation: number;
  externalFragmentation: number;
  blocks: MemoryBlock[];
  pageTables?: Record<string, number[]>;
  segmentTables?: Record<string, SegmentEntry[]>;
}