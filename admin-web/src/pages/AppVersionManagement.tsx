import { useEffect, useRef, useState } from 'react';
import { PackageOpen, Upload, Trash2, ToggleLeft, ToggleRight, AlertTriangle, CheckCircle, Plus, RefreshCw, XCircle } from 'lucide-react';
import api from '../utils/axios';

interface AppVersion {
  id: string;
  versionName: string;
  versionCode: number;
  downloadUrl: string;
  releaseNotes: string;
  isForced: boolean;
  isActive: boolean;
  fileSize: number | null;
  md5: string | null;
  createdAt: string;
}

function formatBytes(bytes: number | null): string {
  if (!bytes) return '—';
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

export default function AppVersionManagement() {
  const [versions, setVersions] = useState<AppVersion[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [toast, setToast] = useState<{ msg: string; type: 'success' | 'error' } | null>(null);

  // Upload state
  const [apkUploading, setApkUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const apkInputRef = useRef<HTMLInputElement>(null);

  // Form state
  const [form, setForm] = useState({
    versionName: '',
    versionCode: '',
    downloadUrl: '',
    releaseNotes: '',
    isForced: false,
    isActive: true,
    fileSize: '',
    md5: '',
  });

  const showToast = (msg: string, type: 'success' | 'error' = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3500);
  };

  const fetchVersions = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/versions');
      setVersions(res.data.data?.versions || []);
    } catch {
      showToast('获取版本列表失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVersions();
  }, []);

  const handleApkFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setApkUploading(true);
    setUploadProgress(0);

    const formData = new FormData();
    formData.append('apk', file);

    try {
      const res = await api.post('/upload/apk', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (evt) => {
          if (evt.total) setUploadProgress(Math.round((evt.loaded * 100) / evt.total));
        },
        timeout: 300_000,
      });
      const { url, fileSize, md5 } = res.data.data;
      setForm((prev) => ({ ...prev, downloadUrl: url, fileSize: String(fileSize), md5: md5 || '' }));
      showToast('APK 上传成功，URL 已自动填入');
    } catch {
      showToast('APK 上传失败，请检查文件格式', 'error');
    } finally {
      setApkUploading(false);
      setUploadProgress(0);
      if (apkInputRef.current) apkInputRef.current.value = '';
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.versionName || !form.versionCode || !form.downloadUrl || !form.releaseNotes) {
      showToast('请填写所有必填字段', 'error');
      return;
    }
    setSubmitting(true);
    try {
      await api.post('/admin/version', {
        versionName: form.versionName,
        versionCode: Number(form.versionCode),
        downloadUrl: form.downloadUrl,
        releaseNotes: form.releaseNotes,
        isForced: form.isForced,
        isActive: form.isActive,
        fileSize: form.fileSize ? Number(form.fileSize) : undefined,
        md5: form.md5 || undefined,
      });
      showToast(`版本 ${form.versionName} 发布成功！`);
      setShowForm(false);
      setForm({ versionName: '', versionCode: '', downloadUrl: '', releaseNotes: '', isForced: false, isActive: true, fileSize: '', md5: '' });
      fetchVersions();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || '发布失败';
      showToast(msg, 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleActive = async (v: AppVersion) => {
    try {
      await api.patch(`/admin/versions/${v.id}`, { isActive: !v.isActive });
      showToast(v.isActive ? '已停用该版本' : '已激活该版本');
      fetchVersions();
    } catch {
      showToast('操作失败', 'error');
    }
  };

  const handleToggleForced = async (v: AppVersion) => {
    try {
      await api.patch(`/admin/versions/${v.id}`, { isForced: !v.isForced });
      showToast(v.isForced ? '已改为可选更新' : '已改为强制更新');
      fetchVersions();
    } catch {
      showToast('操作失败', 'error');
    }
  };

  const handleDelete = async (v: AppVersion) => {
    if (!window.confirm(`确认删除版本 ${v.versionName}？此操作不可恢复。`)) return;
    try {
      await api.delete(`/admin/versions/${v.id}`);
      showToast('删除成功');
      fetchVersions();
    } catch {
      showToast('删除失败', 'error');
    }
  };

  return (
    <div className="flex-col gap-6 relative">
      {/* Toast Notification */}
      {toast && (
        <div className="fixed top-6 right-6 z-50 flex items-center gap-2 p-4 rounded-lg shadow-lg" style={{ background: toast.type === 'success' ? '#e6f4ea' : '#fce8e6', color: toast.type === 'success' ? '#137333' : '#c5221f', border: `1px solid ${toast.type === 'success' ? '#ceead6' : '#fad2cf'}`, transition: 'all 0.3s ease' }}>
          {toast.type === 'success' ? <CheckCircle size={20} /> : <AlertTriangle size={20} />}
          <span className="font-medium text-sm">{toast.msg}</span>
        </div>
      )}

      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold flex items-center gap-3">
            <PackageOpen size={28} style={{ color: 'var(--g-blue)' }} /> 
            App 版本管理 (OTA)
          </h1>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '8px' }}>发布新版本、管理更新策略、控制强制升级</p>
        </div>
        <div className="flex gap-3">
          <button className="md-btn md-btn-outline" onClick={fetchVersions}>
            <RefreshCw size={16} /> 刷新
          </button>
          <button className="md-btn md-btn-primary" onClick={() => setShowForm(true)}>
            <Plus size={16} /> 发布新版本
          </button>
        </div>
      </div>

      <div className="md-card">
        <h2 style={{ fontSize: '16px', fontWeight: 600, margin: 0, padding: '24px 24px 16px', color: 'var(--text-primary)', borderBottom: '1px solid var(--border-color)' }}>
          历史版本列表
        </h2>
        
        {loading ? (
          <div className="md-empty-state">加载中...</div>
        ) : versions.length === 0 ? (
          <div className="md-empty-state">
            <PackageOpen size={48} style={{ color: 'var(--text-secondary)', marginBottom: '16px', opacity: 0.5 }} />
            暂无版本记录，点击“发布新版本”开始
          </div>
        ) : (
          <table className="md-table">
            <thead>
              <tr>
                <th>版本标识</th>
                <th>更新包大小</th>
                <th>发布内容</th>
                <th>分发策略</th>
                <th>当前状态</th>
                <th style={{ textAlign: 'right' }}>操作</th>
              </tr>
            </thead>
            <tbody>
              {versions.map(v => (
                <tr key={v.id}>
                  <td>
                    <div className="flex items-center gap-2">
                      <span style={{ fontWeight: 600, fontSize: '16px', color: 'var(--g-blue)' }}>v{v.versionName}</span>
                      {v.isActive && <span className="badge badge-success" style={{ padding: '2px 6px', fontSize: '10px' }}>Active</span>}
                    </div>
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px', fontFamily: 'monospace' }}>
                      Code: {v.versionCode}
                    </div>
                  </td>
                  <td>
                    <div style={{ fontWeight: 500 }}>{formatBytes(v.fileSize)}</div>
                    {v.md5 && (
                      <div style={{ fontSize: '11px', color: 'var(--text-secondary)', fontFamily: 'monospace', width: '120px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }} title={v.md5}>
                        MD5: {v.md5}
                      </div>
                    )}
                  </td>
                  <td style={{ maxWidth: '300px' }}>
                    <div style={{ fontSize: '13px', color: 'var(--text-primary)', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden', whiteSpace: 'pre-wrap' }}>
                      {v.releaseNotes}
                    </div>
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                      {new Date(v.createdAt).toLocaleDateString()}
                    </div>
                  </td>
                  <td>
                    <button 
                      onClick={() => handleToggleForced(v)}
                      style={{ background: 'none', border: 'none', padding: 0, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                      {v.isForced ? (
                        <ToggleRight size={24} style={{ color: 'var(--g-red)' }} />
                      ) : (
                        <ToggleLeft size={24} style={{ color: 'var(--text-secondary)' }} />
                      )}
                      <span style={{ fontSize: '13px', fontWeight: 500, color: v.isForced ? 'var(--g-red)' : 'var(--text-secondary)' }}>
                        {v.isForced ? '强更激活' : '可选更新'}
                      </span>
                    </button>
                  </td>
                  <td>
                    <button 
                      onClick={() => handleToggleActive(v)}
                      style={{ background: 'none', border: 'none', padding: 0, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                      {v.isActive ? (
                        <ToggleRight size={24} style={{ color: 'var(--g-green)' }} />
                      ) : (
                        <ToggleLeft size={24} style={{ color: 'var(--text-secondary)' }} />
                      )}
                      <span style={{ fontSize: '13px', fontWeight: 500, color: v.isActive ? 'var(--g-green)' : 'var(--text-secondary)' }}>
                        {v.isActive ? '提供下载' : '已停用'}
                      </span>
                    </button>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="flex items-center justify-end gap-2">
                      <a href={v.downloadUrl} target="_blank" rel="noopener noreferrer" className="md-btn-icon" style={{ color: 'var(--g-blue)' }} title="下载安装包">
                        <PackageOpen size={16} />
                      </a>
                      <button className="md-btn-icon" style={{ color: 'var(--g-red)' }} onClick={() => handleDelete(v)} title="永久删除">
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Upload/Publish Modal */}
      {showForm && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(4px)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div className="md-card flex-col" style={{ width: '100%', maxWidth: '600px', maxHeight: '90vh', overflowY: 'auto' }}>
            <div className="flex justify-between items-center" style={{ padding: '24px', borderBottom: '1px solid var(--border-color)' }}>
              <h2 style={{ fontSize: '20px', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>发布新版本</h2>
              <button onClick={() => setShowForm(false)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-secondary)' }}>&times;</button>
            </div>
            
            <form onSubmit={handleSubmit} className="flex-col gap-6" style={{ padding: '24px' }}>
              <div className="grid grid-cols-2 gap-4">
                <div className="flex-col gap-2">
                  <label style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text-secondary)' }}>版本号 (Version Name) *</label>
                  <input type="text" className="md-input" required placeholder="如 8.0.1" value={form.versionName} onChange={e => setForm({...form, versionName: e.target.value})} />
                </div>
                <div className="flex-col gap-2">
                  <label style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text-secondary)' }}>构建号 (Version Code) *</label>
                  <input type="number" className="md-input" required placeholder="如 8000001" value={form.versionCode} onChange={e => setForm({...form, versionCode: e.target.value})} />
                </div>
              </div>

              <div className="flex-col gap-2">
                <label style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text-secondary)' }}>APK 安装包直链 URL *</label>
                <div className="flex gap-2">
                  <input type="url" className="md-input flex-1" required placeholder="请输入以 http 开头的 apk 下载链接" value={form.downloadUrl} onChange={e => setForm({...form, downloadUrl: e.target.value})} />
                  <input type="file" accept=".apk" ref={apkInputRef} style={{ display: 'none' }} onChange={handleApkFileChange} />
                  <button type="button" className="md-btn md-btn-outline flex items-center gap-2" onClick={() => apkInputRef.current?.click()} disabled={apkUploading}>
                    {apkUploading ? <span style={{ fontSize: '13px' }}>上传中 {uploadProgress}%</span> : <><Upload size={16} /> 本地上传</>}
                  </button>
                </div>
                {apkUploading && (
                  <div style={{ width: '100%', height: '4px', background: 'var(--surface-hover)', borderRadius: '2px', overflow: 'hidden', marginTop: '4px' }}>
                    <div style={{ width: `${uploadProgress}%`, height: '100%', background: 'var(--g-blue)', transition: 'width 0.3s' }}></div>
                  </div>
                )}
              </div>

              <div className="flex-col gap-2">
                <label style={{ fontSize: '13px', fontWeight: 500, color: 'var(--text-secondary)' }}>更新日志 (Release Notes) *</label>
                <textarea className="md-input" required rows={4} placeholder="分条列出本次更新的内容..." value={form.releaseNotes} onChange={e => setForm({...form, releaseNotes: e.target.value})}></textarea>
              </div>

              <div className="flex gap-6 p-4" style={{ background: 'var(--surface-hover)', borderRadius: '12px' }}>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" checked={form.isForced} onChange={e => setForm({...form, isForced: e.target.checked})} style={{ width: '16px', height: '16px' }} />
                  <span style={{ fontSize: '14px', fontWeight: 500 }}>是否强制升级 (低于此版本的均强制弹窗)</span>
                </label>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" checked={form.isActive} onChange={e => setForm({...form, isActive: e.target.checked})} style={{ width: '16px', height: '16px' }} />
                  <span style={{ fontSize: '14px', fontWeight: 500 }}>提交后立即激活 (面向客户端)</span>
                </label>
              </div>

              <div className="flex justify-end gap-3 pt-4" style={{ borderTop: '1px solid var(--border-color)' }}>
                <button type="button" className="md-btn md-btn-outline" onClick={() => setShowForm(false)}>取消</button>
                <button type="submit" className="md-btn md-btn-primary" disabled={submitting}>
                  {submitting ? '发布中...' : '确认发布'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
