import { useEffect, useState } from 'react';
import api from '../utils/axios';
import { Layers, Plus, Edit2, Trash2 } from 'lucide-react';

interface Category {
  id: string;
  name: string;
  iconUrl?: string;
  parentId?: string;
  sortOrder: number;
  isLeaf: boolean;
  isActive: boolean;
  attributeSchema: string;
  createdAt: string;
}

export default function CategoryManagement() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editCategory, setEditCategory] = useState<Partial<Category> | null>(null);

  const fetchCategories = () => {
    setLoading(true);
    api.get('/admin/categories')
      .then(res => setCategories(res.data.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const handleOpenModal = (cat?: Category) => {
    if (cat) {
      setEditCategory(cat);
    } else {
      setEditCategory({
        name: '',
        iconUrl: '',
        parentId: '',
        sortOrder: 0,
        isLeaf: true,
        isActive: true,
        attributeSchema: '[]',
      });
    }
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditCategory(null);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editCategory) return;
    
    try {
      JSON.parse(editCategory.attributeSchema || '[]');
    } catch (err) {
      alert('属性 Schema 必须是合法的 JSON 格式');
      return;
    }

    try {
      if (editCategory.id) {
        await api.put(`/admin/categories/${editCategory.id}`, editCategory);
      } else {
        await api.post('/admin/categories', editCategory);
      }
      handleCloseModal();
      fetchCategories();
    } catch (err: any) {
      alert(err.response?.data?.message || '保存失败');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('确认删除此分类吗？关联的子分类和帖子可能会受影响。')) return;
    try {
      await api.delete(`/admin/categories/${id}`);
      fetchCategories();
    } catch (err: any) {
      alert(err.response?.data?.message || '删除失败');
    }
  };

  return (
    <div className="flex-col gap-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-3">
          <Layers size={28} className="text-primary" /> 
          服务类目管理
        </h1>
        <button className="glass-button primary p-2 px-4 flex items-center gap-2" onClick={() => handleOpenModal()}>
          <Plus size={18} /> 新增类目
        </button>
      </div>

      <div className="glass-panel glass-table-container">
        {loading ? (
          <div className="p-8 text-center text-muted">加载中...</div>
        ) : (
          <table className="glass-table">
            <thead>
              <tr>
                <th>类目名称</th>
                <th>图标 / 图标URL</th>
                <th>父级ID</th>
                <th>排序</th>
                <th>叶子节点</th>
                <th>状态</th>
                <th className="text-right">操作</th>
              </tr>
            </thead>
            <tbody>
              {categories.map(cat => (
                <tr key={cat.id}>
                  <td className="font-semibold text-primary">{cat.name}</td>
                  <td>
                    {cat.iconUrl ? (
                      <img src={cat.iconUrl} alt="icon" className="w-8 h-8 object-cover rounded-md border border-gray-200" style={{ width: 32, height: 32 }} />
                    ) : (
                      <span className="text-xs text-muted">无图标</span>
                    )}
                  </td>
                  <td className="text-sm font-mono text-secondary">{cat.parentId || '-'}</td>
                  <td>{cat.sortOrder}</td>
                  <td>
                    <span className={`badge ${cat.isLeaf ? 'badge-primary' : 'badge-neutral'}`}>
                      {cat.isLeaf ? '是' : '否'}
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${cat.isActive ? 'badge-success' : 'badge-danger'}`}>
                      {cat.isActive ? '启用' : '禁用'}
                    </span>
                  </td>
                  <td className="text-right">
                    <div className="flex justify-end gap-2">
                      <button className="glass-button secondary p-2" onClick={() => handleOpenModal(cat)} title="编辑">
                        <Edit2 size={16} className="text-primary" />
                      </button>
                      <button className="glass-button danger p-2" onClick={() => handleDelete(cat.id)} title="删除">
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {categories.length === 0 && (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-muted">暂无类目数据</td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>

      {isModalOpen && editCategory && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="glass-panel p-6" style={{ width: 600, maxHeight: '90vh', overflowY: 'auto' }}>
            <h2 className="text-xl font-bold mb-4">{editCategory.id ? '编辑类目' : '新增类目'}</h2>
            <form onSubmit={handleSave} className="flex-col gap-4">
              
              <div className="flex-col gap-1">
                <label className="text-sm font-medium text-secondary">类目名称 *</label>
                <input 
                  type="text" 
                  className="glass-input" 
                  required 
                  value={editCategory.name || ''} 
                  onChange={e => setEditCategory({ ...editCategory, name: e.target.value })} 
                />
              </div>

              <div className="flex-col gap-1">
                <label className="text-sm font-medium text-secondary">父级 ID (选填)</label>
                <input 
                  type="text" 
                  className="glass-input" 
                  placeholder="留空则为一级类目"
                  value={editCategory.parentId || ''} 
                  onChange={e => setEditCategory({ ...editCategory, parentId: e.target.value })} 
                />
              </div>

              <div className="flex-col gap-1">
                <label className="text-sm font-medium text-secondary">图标 URL</label>
                <div className="flex gap-2">
                  <input 
                    type="text" 
                    className="glass-input flex-1" 
                    value={editCategory.iconUrl || ''} 
                    onChange={e => setEditCategory({ ...editCategory, iconUrl: e.target.value })} 
                  />
                  <input
                    type="file"
                    id="iconUpload"
                    style={{ display: 'none' }}
                    accept="image/*"
                    onChange={async (e) => {
                      const file = e.target.files?.[0];
                      if (!file) return;
                      const formData = new FormData();
                      formData.append('image', file);
                      try {
                        const res = await api.post('/upload/admin', formData, {
                          headers: { 'Content-Type': 'multipart/form-data' }
                        });
                        setEditCategory({ ...editCategory, iconUrl: res.data.data.url });
                      } catch (err: any) {
                        alert(err.response?.data?.message || '图片上传失败');
                      }
                    }}
                  />
                  <button 
                    type="button" 
                    className="glass-button secondary p-2 px-4"
                    onClick={() => document.getElementById('iconUpload')?.click()}
                  >
                    上传图片
                  </button>
                </div>
              </div>

              <div className="flex gap-4">
                <div className="flex-col gap-1 flex-1">
                  <label className="text-sm font-medium text-secondary">排序权重</label>
                  <input 
                    type="number" 
                    className="glass-input" 
                    value={editCategory.sortOrder || 0} 
                    onChange={e => setEditCategory({ ...editCategory, sortOrder: parseInt(e.target.value) || 0 })} 
                  />
                </div>
                
                <div className="flex items-center gap-2 flex-1 mt-6">
                  <input 
                    type="checkbox" 
                    id="isLeaf"
                    checked={editCategory.isLeaf} 
                    onChange={e => setEditCategory({ ...editCategory, isLeaf: e.target.checked })} 
                  />
                  <label htmlFor="isLeaf" className="text-sm font-medium">叶子节点 (允许发帖)</label>
                </div>
                
                <div className="flex items-center gap-2 flex-1 mt-6">
                  <input 
                    type="checkbox" 
                    id="isActive"
                    checked={editCategory.isActive} 
                    onChange={e => setEditCategory({ ...editCategory, isActive: e.target.checked })} 
                  />
                  <label htmlFor="isActive" className="text-sm font-medium text-success">启用状态</label>
                </div>
              </div>

              <div className="flex-col gap-1">
                <label className="text-sm font-medium text-secondary">表单结构 (JSON Schema)</label>
                <textarea 
                  className="glass-input font-mono text-sm" 
                  style={{ height: 160 }}
                  value={editCategory.attributeSchema || ''} 
                  onChange={e => setEditCategory({ ...editCategory, attributeSchema: e.target.value })} 
                />
                <span className="text-xs text-muted">如果不配置表单属性请填 []</span>
              </div>

              <div className="flex justify-end gap-3 mt-4">
                <button type="button" className="glass-button secondary p-2 px-4" onClick={handleCloseModal}>取消</button>
                <button type="submit" className="glass-button primary p-2 px-4">保存设置</button>
              </div>

            </form>
          </div>
        </div>
      )}

    </div>
  );
}
