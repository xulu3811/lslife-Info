import { useEffect, useRef, useState } from 'react';
import { PackageOpen, Upload, Trash2, ToggleLeft, ToggleRight, AlertTriangle, CheckCircle, Plus, RefreshCw } from 'lucide-react';
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

  // APK file upload handler
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
        timeout: 300_000, // 5min for large APK
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
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || '删除失败';
      showToast(msg, 'error');
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Toast */}
      {toast && (
        <div style={{
          position: 'fixed', top: 24, right: 24, zIndex: 9999,
          padding: '12px 20px', borderRadius: 12, fontWeight: 500,
          background: toast.type === 'success' ? '#22c55e' : '#ef4444',
          color: '#fff', boxShadow: '0 8px 24px rgba(0,0,0,0.15)',
          display: 'flex', alignItems: 'center', gap: 8,
          animation: 'fadeIn 0.2s ease',
        }}>
          {toast.type === 'success' ? <CheckCircle size={16} /> : <AlertTriangle size={16} />}
          {toast.msg}
        </div>
      )}

      {/* Header */}
      <div className="glass-panel p-6" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ width: 44, height: 44, borderRadius: 12, background: 'linear-gradient(135deg, #6366f1, #8b5cf6)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <PackageOpen size={22} color="#fff" />
          </div>
          <div>
            <h2 style={{ margin: 0, fontSize: 18, fontWeight: 700 }}>App 版本管理 (OTA)</h2>
            <p style={{ margin: 0, fontSize: 13, color: 'var(--text-secondary)' }}>发布新版本 · 管理更新策略 · 控制强制升级</p>
          </div>
        </div>
        <div style={{ display: 'flex', gap: 10 }}>
          <button
            onClick={fetchVersions}
            className="flex items-center gap-2"
            style={{ padding: '8px 16px', borderRadius: 8, border: '1px solid var(--surface-border)', background: 'transparent', cursor: 'pointer', color: 'var(--text-secondary)', fontWeight: 500 }}
          >
            <RefreshCw size={14} /> 刷新
          </button>
          <button
            onClick={() => setShowForm(true)}
            className="flex items-center gap-2"
            style={{ padding: '8px 20px', borderRadius: 8, border: 'none', background: 'linear-gradient(135deg, #6366f1, #8b5cf6)', color: '#fff', cursor: 'pointer', fontWeight: 600, boxShadow: '0 4px 12px rgba(99,102,241,0.3)' }}
          >
            <Plus size={16} /> 发布新版本
          </button>
        </div>
      </div>

      {/* Publish Form Modal */}
      {showForm && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 24 }}
          onClick={(e) => { if (e.target === e.currentTarget) setShowForm(false); }}
        >
          <div className="glass-panel" style={{ width: '100%', maxWidth: 600, maxHeight: '90vh', overflowY: 'auto', padding: 32 }}>
            <h3 style={{ margin: '0 0 24px', fontSize: 18, fontWeight: 700 }}>🚀 发布新版本</h3>
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                <div>
                  <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 6 }}>版本名称 <span style={{ color: '#ef4444' }}>*</span></label>
                  <input
                    value={form.versionName}
                    onChange={e => setForm(p => ({ ...p, versionName: e.target.value }))}
                    placeholder="例: 5.02"
                    required
                    style={{ width: '100%', padding: '10px 14px', borderRadius: 8, border: '1px solid var(--surface-border)', background: 'var(--surface)', color: 'var(--text)', fontSize: 14, boxSizing: 'border-box' }}
                  />
                </div>
                <div>
                  <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 6 }}>版本号 (versionCode) <span style={{ color: '#ef4444' }}>*</span></label>
                  <input
                    type="number"
                    value={form.versionCode}
                    onChange={e => setForm(p => ({ ...p, versionCode: e.target.value }))}
                    placeholder="例: 502"
                    required
                    style={{ width: '100%', padding: '10px 14px', borderRadius: 8, border: '1px solid var(--surface-border)', background: 'var(--surface)', color: 'var(--text)', fontSize: 14, boxSizing: 'border-box' }}
                  />
                </div>
              </div>

              {/* APK Upload */}
              <div>
                <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 6 }}>上传 APK 文件</label>
                <div
                  onClick={() => apkInputRef.current?.click()}
                  style={{
                    border: '2px dashed var(--surface-border)', borderRadius: 10, padding: '20px 16px',
                    textAlign: 'center', cursor: apkUploading ? 'not-allowed' : 'pointer',
                    background: 'var(--surface)', transition: 'border-color 0.2s',
                    opacity: apkUploading ? 0.7 : 1,
                  }}
                >
                  <Upload size={24} color="var(--text-secondary)" style={{ margin: '0 auto 8px' }} />
                  {apkUploading ? (
                    <div>
                      <p style={{ margin: '0 0 8px', fontSize: 13, color: 'var(--text-secondary)' }}>上传中... {uploadProgress}%</p>
                      <div style={{ height: 6, background: 'var(--surface-border)', borderRadius: 3 }}>
                        <div style={{ width: `${uploadProgress}%`, height: '100%', background: 'linear-gradient(90deg, #6366f1, #8b5cf6)', borderRadius: 3, transition: 'width 0.3s' }} />
                      </div>
                    </div>
                  ) : (
                    <p style={{ margin: 0, fontSize: 13, color: 'var(--text-secondary)' }}>
                      点击选择 APK 文件（最大 150MB）<br />
                      <span style={{ fontSize: 11, color: '#6366f1' }}>上传后将自动填入下载URL、文件大小、MD5</span>
                    </p>
                  )}
                </div>
                <input ref={apkInputRef} type="file" accept=".apk,application/vnd.android.package-archive" style={{ display: 'none' }} onChange={handleApkFileChange} />
              </div>

              <div>
                <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 6 }}>APK 下载 URL <span style={{ color: '#ef4444' }}>*</span></label>
                <input
                  value={form.downloadUrl}
                  onChange={e => setForm(p => ({ ...p, downloadUrl: e.target.value }))}
                  placeholder="https://mentalhlp.site/apks/lslife_xxx.apk"
                  required
                  style={{ width: '100%', padding: '10px 14px', borderRadius: 8, border: '1px solid var(--surface-border)', background: 'var(--surface)', color: 'var(--text)', fontSize: 14, boxSizing: 'border-box' }}
                />
              </div>

              <div>
                <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 6 }}>更新日志 <span style={{ color: '#ef4444' }}>*</span></label>
                <textarea
                  value={form.releaseNotes}
                  onChange={e => setForm(p => ({ ...p, releaseNotes: e.target.value }))}
                  placeholder={'1. 修复了若干已知问题\n2. 优化了启动速度\n3. 新增版本管理功能'}
                  required
                  rows={4}
                  style={{ width: '100%', padding: '10px 14px', borderRadius: 8, border: '1px solid var(--surface-border)', background: 'var(--surface)', color: 'var(--text)', fontSize: 14, resize: 'vertical', boxSizing: 'border-box' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                <div>
                  <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 6 }}>文件大小（字节）</label>
                  <input
                    type="number"
                    value={form.fileSize}
                    onChange={e => setForm(p => ({ ...p, fileSize: e.target.value }))}
                    placeholder="自动填入"
                    style={{ width: '100%', padding: '10px 14px', borderRadius: 8, border: '1px solid var(--surface-border)', background: 'var(--surface)', color: 'var(--text)', fontSize: 14, boxSizing: 'border-box' }}
                  />
                </div>
                <div>
                  <label style={{ fontSize: 13, fontWeight: 600, display: 'block', marginBottom: 6 }}>MD5 校验</label>
                  <input
                    value={form.md5}
                    onChange={e => setForm(p => ({ ...p, md5: e.target.value }))}
                    placeholder="自动填入"
                    style={{ width: '100%', padding: '10px 14px', borderRadius: 8, border: '1px solid var(--surface-border)', background: 'var(--surface)', color: 'var(--text)', fontSize: 14, boxSizing: 'border-box' }}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', gap: 16 }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', fontWeight: 500, fontSize: 14 }}>
                  <input type="checkbox" checked={form.isForced} onChange={e => setForm(p => ({ ...p, isForced: e.target.checked }))} />
                  <AlertTriangle size={14} color="#f59e0b" /> 强制更新（用户必须升级）
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', fontWeight: 500, fontSize: 14 }}>
                  <input type="checkbox" checked={form.isActive} onChange={e => setForm(p => ({ ...p, isActive: e.target.checked }))} />
                  <CheckCircle size={14} color="#22c55e" /> 立即激活（作为当前下发版本）
                </label>
              </div>

              <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
                <button
                  type="button"
                  onClick={() => setShowForm(false)}
                  style={{ flex: 1, padding: '11px 0', borderRadius: 8, border: '1px solid var(--surface-border)', background: 'transparent', color: 'var(--text)', cursor: 'pointer', fontWeight: 500 }}
                >
                  取消
                </button>
                <button
                  type="submit"
                  disabled={submitting || apkUploading}
                  style={{ flex: 2, padding: '11px 0', borderRadius: 8, border: 'none', background: 'linear-gradient(135deg, #6366f1, #8b5cf6)', color: '#fff', cursor: 'pointer', fontWeight: 600, opacity: submitting ? 0.7 : 1 }}
                >
                  {submitting ? '发布中...' : '🚀 确认发布'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Version List */}
      <div className="glass-panel" style={{ overflow: 'hidden' }}>
        <div style={{ padding: '20px 24px', borderBottom: '1px solid var(--surface-border)' }}>
          <h3 style={{ margin: 0, fontSize: 15, fontWeight: 600 }}>历史版本列表</h3>
        </div>

        {loading ? (
          <div style={{ padding: 48, textAlign: 'center', color: 'var(--text-secondary)' }}>加载中...</div>
        ) : versions.length === 0 ? (
          <div style={{ padding: 48, textAlign: 'center', color: 'var(--text-secondary)' }}>
            <PackageOpen size={40} style={{ opacity: 0.3, margin: '0 auto 12px' }} />
            <p>暂无版本记录，点击"发布新版本"开始</p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
              <thead>
                <tr style={{ background: 'var(--surface)', borderBottom: '1px solid var(--surface-border)' }}>
                  {['版本名称', '版本号', '文件大小', '状态', '强制', '更新日志', '发布时间', '操作'].map(h => (
                    <th key={h} style={{ padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: 'var(--text-secondary)', whiteSpace: 'nowrap' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {versions.map((v, i) => (
                  <tr key={v.id} style={{ borderBottom: i < versions.length - 1 ? '1px solid var(--surface-border)' : 'none', transition: 'background 0.15s' }}>
                    <td style={{ padding: '14px 16px', fontWeight: 700 }}>
                      <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        {v.isActive && <span style={{ background: '#22c55e', color: '#fff', fontSize: 11, padding: '2px 7px', borderRadius: 20, fontWeight: 600 }}>当前</span>}
                        v{v.versionName}
                      </span>
                    </td>
                    <td style={{ padding: '14px 16px', fontFamily: 'monospace', color: 'var(--text-secondary)' }}>{v.versionCode}</td>
                    <td style={{ padding: '14px 16px', color: 'var(--text-secondary)' }}>{formatBytes(v.fileSize)}</td>
                    <td style={{ padding: '14px 16px' }}>
                      <button
                        onClick={() => handleToggleActive(v)}
                        style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4, color: v.isActive ? '#22c55e' : 'var(--text-secondary)' }}
                        title={v.isActive ? '点击停用' : '点击激活'}
                      >
                        {v.isActive ? <ToggleRight size={22} /> : <ToggleLeft size={22} />}
                        <span style={{ fontSize: 12 }}>{v.isActive ? '激活' : '停用'}</span>
                      </button>
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <button
                        onClick={() => handleToggleForced(v)}
                        style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4, color: v.isForced ? '#f59e0b' : 'var(--text-secondary)' }}
                        title={v.isForced ? '点击改为可选' : '点击改为强制'}
                      >
                        <AlertTriangle size={16} />
                        <span style={{ fontSize: 12 }}>{v.isForced ? '强制' : '可选'}</span>
                      </button>
                    </td>
                    <td style={{ padding: '14px 16px', maxWidth: 220 }}>
                      <p style={{ margin: 0, fontSize: 12, color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={v.releaseNotes}>
                        {v.releaseNotes}
                      </p>
                    </td>
                    <td style={{ padding: '14px 16px', color: 'var(--text-secondary)', whiteSpace: 'nowrap', fontSize: 12 }}>
                      {new Date(v.createdAt).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })}
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      <div style={{ display: 'flex', gap: 8 }}>
                        <a
                          href={v.downloadUrl}
                          target="_blank"
                          rel="noreferrer"
                          title="下载 APK"
                          style={{ padding: '6px 10px', borderRadius: 6, background: 'rgba(99,102,241,0.1)', color: '#6366f1', textDecoration: 'none', display: 'flex', alignItems: 'center' }}
                        >
                          <Upload size={14} />
                        </a>
                        <button
                          onClick={() => handleDelete(v)}
                          disabled={v.isActive}
                          title={v.isActive ? '不能删除激活版本' : '删除版本'}
                          style={{ padding: '6px 10px', borderRadius: 6, background: v.isActive ? 'transparent' : 'rgba(239,68,68,0.1)', color: v.isActive ? 'var(--text-secondary)' : '#ef4444', border: 'none', cursor: v.isActive ? 'not-allowed' : 'pointer', display: 'flex', alignItems: 'center', opacity: v.isActive ? 0.4 : 1 }}
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
