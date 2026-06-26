import React, { useState, useEffect } from 'react';
import type { MemorySnapshot, SegmentEntry } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export const MemoryMap: React.FC = () => {
  const [snapshot, setSnapshot] = useState<MemorySnapshot | null>(null);
  const [procId, setProcId] = useState('');
  const [size, setSize] = useState(0);

  // Initialize with Dynamic Best-Fit by default on mount
  useEffect(() => {
    handleInitialize('best-fit');
  }, []);

  const handleInitialize = async (strategy: string) => {
    const response = await fetch(`${API_BASE_URL}/api/v1/memory/init?strategy=${strategy}&totalSize=1024`, {
      method: 'POST'
    });
    const data = await response.json();
    setSnapshot(data);
  };

  const handleAllocate = async () => {
    if (!procId || size <= 0) {
      alert("Please provide a valid Process ID and Size greater than 0.");
      return;
    }
    const response = await fetch(`${API_BASE_URL}/api/v1/memory/allocate?processId=${procId}&size=${size}`, {
      method: 'POST'
    });
    const data = await response.json();
    setSnapshot(data);
    setProcId('');
    setSize(0);
  };

  const handleDeallocate = async () => {
    if (!procId) {
      alert("Please provide a Process ID to deallocate.");
      return;
    }
    const response = await fetch(`${API_BASE_URL}/api/v1/memory/deallocate?processId=${procId}`, {
      method: 'POST'
    });
    const data = await response.json();
    setSnapshot(data);
    setProcId('');
  };

  const handleRefresh = async () => {
    const response = await fetch(`${API_BASE_URL}/api/v1/memory/state`);
    const data = await response.json();
    setSnapshot(data);
  };

  return (
    <div style={{ fontFamily: 'Arial, sans-serif', padding: '30px', maxWidth: '1200px', margin: '0 auto' }}>
      <h2>Memory Allocation System Visualizer</h2>
      
      {/* Configuration Strategy Toggles */}
      <div style={{ marginBottom: '20px', padding: '15px', backgroundColor: '#f8f9fa', borderRadius: '4px', border: '1px solid #dee2e6' }}>
        <strong style={{ marginRight: '15px' }}>Select OS Strategy:</strong>
        <button 
          onClick={() => handleInitialize('fixed')} 
          style={{ 
            padding: '8px 16px', 
            backgroundColor: snapshot?.strategy === 'fixed' ? '#005088' : '#e9ecef', 
            color: snapshot?.strategy === 'fixed' ? 'white' : '#333',
            border: '1px solid #ccc',
            borderRadius: '4px',
            cursor: 'pointer',
            fontWeight: snapshot?.strategy === 'fixed' ? 'bold' : 'normal'
          }}
        >
          Fixed Partitioning (4x256KB)
        </button>
        <button 
          onClick={() => handleInitialize('best-fit')} 
          style={{ 
            marginLeft: '10px',
            padding: '8px 16px', 
            backgroundColor: snapshot?.strategy === 'best-fit' ? '#005088' : '#e9ecef', 
            color: snapshot?.strategy === 'best-fit' ? 'white' : '#333',
            border: '1px solid #ccc',
            borderRadius: '4px',
            cursor: 'pointer',
            fontWeight: snapshot?.strategy === 'best-fit' ? 'bold' : 'normal'
          }}
        >
          Dynamic Partitioning (Best-Fit)
        </button>
        <button 
          onClick={() => handleInitialize('paging')} 
          style={{ 
            marginLeft: '10px',
            padding: '8px 16px', 
            backgroundColor: snapshot?.strategy === 'paging' ? '#005088' : '#e9ecef', 
            color: snapshot?.strategy === 'paging' ? 'white' : '#333',
            border: '1px solid #ccc',
            borderRadius: '4px',
            cursor: 'pointer',
            fontWeight: snapshot?.strategy === 'paging' ? 'bold' : 'normal'
          }}
        >
          Paging (64KB Frames)
        </button>
        <button 
          onClick={() => handleInitialize('segmentation')} 
          style={{ 
            marginLeft: '10px',
            padding: '8px 16px', 
            backgroundColor: snapshot?.strategy === 'segmentation' ? '#005088' : '#e9ecef', 
            color: snapshot?.strategy === 'segmentation' ? 'white' : '#333',
            border: '1px solid #ccc',
            borderRadius: '4px',
            cursor: 'pointer',
            fontWeight: snapshot?.strategy === 'segmentation' ? 'bold' : 'normal'
          }}
        >
          Segmentation
        </button>
      </div>

      {/* Control Inputs Panel */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '10px', alignItems: 'center' }}>
        <input 
          placeholder="Process ID (e.g., P1)" 
          value={procId}
          onChange={e => setProcId(e.target.value)} 
          style={{ padding: '8px', border: '1px solid #ccc', borderRadius: '4px', width: '180px' }}
        />
        <input 
          type="number" 
          placeholder="Size (KB)" 
          value={size || ''}
          onChange={e => setSize(parseInt(e.target.value) || 0)} 
          style={{ padding: '8px', border: '1px solid #ccc', borderRadius: '4px', width: '120px' }}
        />
        <button onClick={handleAllocate} style={{ padding: '8px 16px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Allocate</button>
        <button onClick={handleDeallocate} style={{ padding: '8px 16px', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Deallocate</button>
        <button onClick={handleRefresh} style={{ padding: '8px 16px', backgroundColor: '#6c757d', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Refresh State</button>
      </div>

      {/* System Live Performance Indicators */}
      <div style={{ marginBottom: '15px', padding: '12px', backgroundColor: '#e9ecef', borderRadius: '4px', display: 'flex', gap: '20px', fontSize: '15px' }}>
        <div><strong>Strategy Rule:</strong> <span style={{ textTransform: 'uppercase', color: '#005088' }}>{snapshot?.strategy ?? '-'}</span></div>
        <div><strong>Total RAM:</strong> {snapshot?.totalSize ?? 1024} KB</div>
        <div><strong>Used Space:</strong> {snapshot?.usedSize ?? 0} KB ({(((snapshot?.usedSize ?? 0) / (snapshot?.totalSize || 1024)) * 100).toFixed(1)}%)</div>
        <div><strong>Internal Fragmentation:</strong> <span style={{ color: (snapshot?.internalFragmentation ?? 0) > 0 ? '#dc3545' : '#333' }}>{snapshot?.internalFragmentation ?? 0} KB</span></div>
        <div><strong>External Fragmentation:</strong> {snapshot?.externalFragmentation ?? 0} KB</div>
      </div>

      {/* Primary Visual Representation Horizontal Container */}
      <div style={{ display: 'flex', width: '100%', height: '80px', border: '2px solid #1a1d23', borderRadius: '6px', overflow: 'hidden', backgroundColor: '#fff', marginBottom: '25px' }}>
        {snapshot?.blocks && snapshot.blocks.length > 0 ? (
          snapshot.blocks.map((block) => {
            const widthPct = (block.size / snapshot.totalSize) * 100;
            return (
              <div
                key={block.id}
                style={{
                  width: `${widthPct}%`,
                  backgroundColor: block.isAllocated ? '#005088' : '#e9ecef',
                  color: block.isAllocated ? 'white' : '#495057',
                  borderRight: '1px solid #fff',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'center',
                  alignItems: 'center',
                  fontSize: '13px',
                  fontWeight: '500',
                  transition: 'width 0.3s ease, background-color 0.3s ease',
                  overflow: 'hidden',
                  whiteSpace: 'nowrap',
                  textOverflow: 'ellipsis',
                  padding: '0 4px'
                }}
                title={`Block Start Address: ${block.startAddress}KB | Size: ${block.size}KB`}
              >
                <span>{block.isAllocated ? block.processId : 'Free'}</span>
                <span style={{ fontSize: '11px', opacity: 0.85 }}>{block.size} KB</span>
              </div>
            );
          })
        ) : (
          <div style={{ width: '100%', textAlign: 'center', lineHeight: '80px', color: '#6c757d' }}>
            System offline. Please initialize memory environment layout.
          </div>
        )}
      </div>

      {/* Context-Aware Translation Tables Panel (Shows Paging/Segmentation data) */}
      <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
        {/* Render Page Tables side panel if strategy is paging */}
        {snapshot?.strategy === 'paging' && snapshot.pageTables && Object.keys(snapshot.pageTables).length > 0 && (
          <div style={{ flex: 1, padding: '15px', border: '1px solid #dee2e6', borderRadius: '4px', backgroundColor: '#f8f9fa' }}>
            <h4 style={{ margin: '0 0 10px 0', color: '#005088' }}>Kernel Page Tables Mapping</h4>
            {Object.entries(snapshot.pageTables).map(([procId, frames]) => (
              <div key={procId} style={{ marginBottom: '8px', fontSize: '14px' }}>
                <strong>Process {procId}:</strong>
                <div style={{ display: 'flex', gap: '5px', marginTop: '4px' }}>
                  {(frames as number[]).map((frame: number, pageIdx: number) => (
                    <span key={pageIdx} style={{ padding: '3px 8px', backgroundColor: '#005088', color: 'white', borderRadius: '3px', fontSize: '12px' }}>
                      Page {pageIdx} → Frame {frame}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Render Segment Tables side panel if strategy is segmentation */}
        {snapshot?.strategy === 'segmentation' && snapshot.segmentTables && Object.keys(snapshot.segmentTables).length > 0 && (
          <div style={{ flex: 1, padding: '15px', border: '1px solid #dee2e6', borderRadius: '4px', backgroundColor: '#f8f9fa' }}>
            <h4 style={{ margin: '0 0 10px 0', color: '#005088' }}>Logical Segment Descriptors</h4>
            {Object.entries(snapshot.segmentTables).map(([procId, segments]) => (
              <div key={procId} style={{ marginBottom: '12px', fontSize: '14px', borderBottom: '1px solid #e9ecef', paddingBottom: '8px' }}>
                <strong>Process {procId}:</strong>
                <table style={{ width: '100%', marginTop: '5px', borderCollapse: 'collapse', fontSize: '12px' }}>
                  <thead>
                    <tr style={{ textAlign: 'left', borderBottom: '1px solid #ccc' }}>
                      <th>Segment</th>
                      <th>Base Address</th>
                      <th>Limit (Size)</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(segments as SegmentEntry[]).map((seg: SegmentEntry, idx: number) => (
                      <tr key={idx}>
                        <td style={{ padding: '4px 0' }}>{seg.name}</td>
                        <td>{seg.baseAddress} KB</td>
                        <td>{seg.limit} KB</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};