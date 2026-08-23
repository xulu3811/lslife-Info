import { useEffect, useState } from 'react';
import { ShoppingBag, Search, Ban, CheckCircle } from 'lucide-react';
import api from '../utils/axios';

interface Product {
  id: string;
  name: string;
  desc: string;
  price: number;
  sales: number;
  status: string;
  merchant?: { name: string };
  createdAt: string;
}

export function ProductAudit() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/products', { params: { search, status } });
      setProducts(res.data.data.list);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [search, status]);

  const handleStatusChange = async (id: string, newStatus: string) => {
    if (!window.confirm(`确认将商品状态修改为 ${newStatus === 'active' ? '正常' : '违规下架'}?`)) return;
    try {
      await api.post(`/admin/products/${id}/status`, { status: newStatus });
      fetchProducts();
    } catch (e: any) {
      alert(e.response?.data?.message || '操作失败');
    }
  };

  return (
    <div className="flex-col gap-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-3">
          <ShoppingBag size={28} className="text-primary" /> 
          全局商品监管库
        </h1>
      </div>

      <div className="glass-panel p-6 flex flex-wrap gap-4 items-center mb-6">
        <div className="flex-1 relative">
          <div style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }}>
            <Search size={18} />
          </div>
          <input
            type="text"
            placeholder="搜索商品名/描述..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="glass-input"
            style={{ paddingLeft: '40px' }}
          />
        </div>
        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          className="glass-input"
          style={{ width: '200px' }}
        >
          <option value="">全部状态</option>
          <option value="active">正常在售</option>
          <option value="offline">已下架</option>
        </select>
      </div>

      <div className="glass-panel glass-table-container">
        {loading ? (
          <div className="p-8 text-center text-muted">加载中...</div>
        ) : (
          <table className="glass-table">
            <thead>
              <tr>
                <th>商品名称</th>
                <th>所属商户</th>
                <th>价格/销量</th>
                <th>状态</th>
                <th className="text-right">干预操作</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td>
                    <div className="font-semibold">{product.name}</div>
                    <div className="text-xs text-secondary mt-2 truncate" style={{ maxWidth: '250px' }} title={product.desc}>{product.desc}</div>
                  </td>
                  <td className="font-medium">{product.merchant?.name || '未知商户'}</td>
                  <td>
                    <div className="text-danger font-bold text-lg">¥ {product.price.toFixed(2)}</div>
                    <div className="text-sm text-secondary mt-2">已售 {product.sales}</div>
                  </td>
                  <td>
                    <span className={`badge ${product.status === 'active' ? 'badge-active' : 'badge-danger'}`}>
                      {product.status === 'active' ? '正常在售' : '强制下架'}
                    </span>
                  </td>
                  <td className="text-right">
                    {product.status === 'active' ? (
                      <button onClick={() => handleStatusChange(product.id, 'offline')} className="glass-button danger" title="强制下架">
                        <Ban size={18} /> 违规下架
                      </button>
                    ) : (
                      <button onClick={() => handleStatusChange(product.id, 'active')} className="glass-button success" title="解除限制">
                        <CheckCircle size={18} /> 解除限制
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {products.length === 0 && (
                <tr><td colSpan={5} className="p-8 text-center text-muted">暂无商品</td></tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
