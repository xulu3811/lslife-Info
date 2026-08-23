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
          <MessageSquareWarning size={28} className="text-primary" /> 
          C2C 闲置与服务审核
        </h1>
      </div>

      <div className="glass-panel p-6 mb-6 flex gap-4 items-center">
        <h2 className="text-lg font-medium m-0 text-secondary mr-4">审核队列</h2>
        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          className="glass-input"
          style={{ width: '240px' }}
        >
          <option value="pending_review">待审核排队中</option>
          <option value="published">已发布 (已通过)</option>
          <option value="rejected">已驳回记录</option>
        </select>
      </div>

      <div className="glass-panel glass-table-container">
        {loading ? (
          <div className="p-8 text-center text-muted">加载中...</div>
        ) : (
          <table className="glass-table">
            <thead>
              <tr>
                <th>发布者</th>
                <th>内容详情</th>
                <th>图集</th>
                <th>提交时间</th>
                <th>当前状态</th>
                <th className="text-right">审核操作</th>
              </tr>
            </thead>
            <tbody>
              {posts.map((post) => (
                <tr key={post.id}>
                  <td>
                    <div className="font-semibold">{post.user?.nickname}</div>
                    <div className="text-xs text-secondary mt-1">{post.user?.phone}</div>
                  </td>
                  <td style={{ maxWidth: '300px' }}>
                    <div className="font-semibold text-base mb-2">
                      <span className="badge badge-neutral mr-2">
                        {categoryMap[post.category] || post.category.replace('cat_', '')}
                      </span>
                      {post.title}
                    </div>
                    <div className="text-sm text-secondary truncate" style={{ maxWidth: '280px' }} title={post.description}>
                      {post.description}
                    </div>
                    {post.price !== undefined && post.price !== null && (
                      <div className="text-danger font-bold mt-2">
                        ¥ {post.price}
                      </div>
                    )}
                  </td>
                  <td>
                    <div className="flex gap-2">
                      {post.images && post.images.slice(0, 3).map((img, i) => (
                        <img key={i} src={img} alt="post" style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 8, border: '1px solid var(--surface-border)' }} />
                      ))}
                      {post.images && post.images.length > 3 && (
                        <div className="text-xs font-semibold text-secondary flex items-center justify-center" style={{ width: 48, height: 48, borderRadius: 8, background: 'rgba(0,0,0,0.05)' }}>
                          +{post.images.length - 3}
                        </div>
                      )}
                    </div>
                  </td>
                  <td className="text-sm text-secondary">
                    {new Date(post.createdAt).toLocaleString()}
                  </td>
                  <td>
                    <span className={`badge ${post.status === 'published' ? 'badge-success' : post.status === 'pending_review' ? 'badge-warning' : 'badge-danger'}`}>
                      {post.status === 'published' ? '已发布' : post.status === 'pending_review' ? '审核中' : '已驳回'}
                    </span>
                  </td>
                  <td className="text-right">
                    {post.status === 'pending_review' && (
                      <div className="flex justify-end gap-2">
                        <button className="glass-button success p-2 px-3" onClick={() => handleAudit(post.id, 'approve')}>
                          <Check size={16} /> 通过
                        </button>
                        <button className="glass-button danger p-2 px-3" onClick={() => handleAudit(post.id, 'reject')}>
                          <X size={16} /> 驳回
                        </button>
                      </div>
                    )}
                    {post.status === 'published' && (
                      <div className="flex justify-end gap-2">
                        <button className="glass-button danger p-2 px-3" onClick={() => handleAudit(post.id, 'ban')}>
                          <Ban size={16} /> 违规下架
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
              {posts.length === 0 && (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-muted">暂无帖子数据</td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
