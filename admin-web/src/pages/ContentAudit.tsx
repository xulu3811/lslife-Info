import { useEffect, useState } from 'react';
import { MessageSquareWarning, Check, X, Ban } from 'lucide-react';
import api from '../utils/axios';

interface Post {
  id: string;
  category: string;
  title: string;
  description: string;
  price?: number;
  images: string[];
  status: string;
  createdAt: string;
  user: { nickname: string; phone: string };
}

export default function ContentAudit() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [categoryMap, setCategoryMap] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState('pending_review');

  const fetchPostsAndCategories = async () => {
    setLoading(true);
    try {
      const [postsRes, catRes] = await Promise.all([
        api.get('/admin/posts', { params: { status } }),
        api.get('/admin/categories')
      ]);
      setPosts(postsRes.data.data?.list || postsRes.data.data || []);
      
      const cats = catRes.data.data || [];
      const map: Record<string, string> = {};
      cats.forEach((c: any) => {
        map[c.id] = c.name;
      });
      setCategoryMap(map);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPostsAndCategories();
  }, [status]);

  const handleAudit = async (id: string, action: 'approve' | 'reject' | 'ban') => {
    let note = '';
    if (action === 'reject' || action === 'ban') {
      const input = window.prompt(`请输入${action === 'ban' ? '下架' : '驳回'}原因:`);
      if (input === null) return;
      note = input;
    } else {
      if (!window.confirm('确认通过该内容发布？')) return;
    }

    try {
      await api.post(`/admin/posts/${id}/audit`, { action, note });
      fetchPostsAndCategories();
    } catch (e: any) {
      alert(e.response?.data?.message || '审核操作失败');
    }
  };

  return (
    <div className="flex-col gap-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-3">
          <MessageSquareWarning size={28} style={{ color: 'var(--g-blue)' }} /> 
          C2C 闲置与服务审核
        </h1>
      </div>

      <div className="md-card flex items-center gap-4 mb-6" style={{ padding: '16px 24px' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 500, margin: 0, color: 'var(--text-secondary)' }}>审核队列</h2>
        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          className="md-input"
          style={{ width: '240px' }}
        >
          <option value="pending_review">待审核排队中</option>
          <option value="published">已发布 (已通过)</option>
          <option value="rejected">已驳回记录</option>
        </select>
      </div>

      <div className="md-card md-table-container">
        {loading ? (
          <div className="md-empty-state">加载中...</div>
        ) : posts.length === 0 ? (
          <div className="md-empty-state">暂无帖子数据</div>
        ) : (
          <table className="md-table">
            <thead>
              <tr>
                <th>发布者</th>
                <th>内容详情</th>
                <th>图集</th>
                <th>提交时间</th>
                <th>当前状态</th>
                <th style={{ textAlign: 'right' }}>审核操作</th>
              </tr>
            </thead>
            <tbody>
              {posts.map((post) => (
                <tr key={post.id}>
                  <td>
                    <div style={{ fontWeight: 500, color: 'var(--text-primary)' }}>{post.user?.nickname}</div>
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>{post.user?.phone}</div>
                  </td>
                  <td style={{ maxWidth: '300px' }}>
                    <div style={{ fontWeight: 500, fontSize: '15px', marginBottom: '8px' }}>
                      <span className="badge badge-success" style={{ marginRight: '8px' }}>
                        {categoryMap[post.category] || post.category.replace('cat_', '')}
                      </span>
                      {post.title}
                    </div>
                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '280px' }} title={post.description}>
                      {post.description}
                    </div>
                    {post.price !== undefined && post.price !== null && (
                      <div style={{ color: 'var(--g-red)', fontWeight: 'bold', marginTop: '8px' }}>
                        ¥ {post.price}
                      </div>
                    )}
                  </td>
                  <td>
                    <div className="flex gap-2">
                      {post.images && post.images.slice(0, 3).map((img, i) => (
                        <img key={i} src={img.startsWith('http') ? img : (img.startsWith('/') ? `${window.location.origin}${img}` : `${window.location.origin}/${img}`)} alt="post" style={{ width: '48px', height: '48px', objectFit: 'cover', borderRadius: '8px', border: '1px solid var(--border-color)' }} />
                      ))}
                      {post.images && post.images.length > 3 && (
                        <div style={{ width: '48px', height: '48px', borderRadius: '8px', background: 'var(--surface-hover)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)' }}>
                          +{post.images.length - 3}
                        </div>
                      )}
                    </div>
                  </td>
                  <td style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                    {new Date(post.createdAt).toLocaleString()}
                  </td>
                  <td>
                    <span className={`badge ${post.status === 'published' || post.status === 'PUBLISHED' ? 'badge-success' : ['pending_review', 'MANUAL_REVIEWING', 'AI_REVIEWING'].includes(post.status) ? 'badge-warning' : 'badge-danger'}`}>
                      {post.status === 'published' || post.status === 'PUBLISHED' ? '已发布' : ['pending_review', 'MANUAL_REVIEWING', 'AI_REVIEWING'].includes(post.status) ? '审核中' : '已驳回'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    {['pending_review', 'MANUAL_REVIEWING', 'AI_REVIEWING'].includes(post.status) && (
                      <div className="flex items-center justify-end gap-2">
                        <button className="md-btn md-btn-primary" style={{ padding: '4px 12px' }} onClick={() => handleAudit(post.id, 'approve')}>
                          <Check size={16} /> 通过
                        </button>
                        <button className="md-btn md-btn-danger" style={{ padding: '4px 12px' }} onClick={() => handleAudit(post.id, 'reject')}>
                          <X size={16} /> 驳回
                        </button>
                      </div>
                    )}
                    {(post.status === 'published' || post.status === 'PUBLISHED') && (
                      <button className="md-btn md-btn-danger" style={{ padding: '4px 12px' }} onClick={() => handleAudit(post.id, 'ban')}>
                        <Ban size={16} /> 下架违规
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
