import { useEffect, useState } from 'react';
import { Receipt, Search, RefreshCw, Send, CheckCircle, XCircle } from 'lucide-react';
import api from '../utils/axios';

interface OrderItem {
  id: string;
  name: string;
  price: number;
  quantity: number;
}

interface Order {
  id: string;
  orderNo: string;
  status: string;
  totalAmount: number;
  merchantName: string;
  createdAt: string;
  items: OrderItem[];
  user: { nickname: string; phone: string };
}

export function OrderManagement() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/orders', { params: { search, status } });
      setOrders(res.data.data?.list || []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [search, status]);

  const handleAction = async (id: string, action: 'refund' | 'assign_rider' | 'complete', confirmMsg: string) => {
    if (!window.confirm(confirmMsg)) return;
    try {
      await api.post(`/admin/orders/${id}/action`, { action });
      fetchOrders();
      alert('操作成功');
    } catch (e: any) {
      alert(e.response?.data?.message || '操作失败');
    }
  };

  const getStatusBadge = (status: string) => {
    switch(status) {
      case 'pending': return <span className="badge badge-warning">待支付</span>;
      case 'paid': return <span className="badge badge-info">已支付</span>;
      case 'preparing': return <span className="badge badge-warning">备餐中</span>;
      case 'delivering': return <span className="badge badge-info">配送中</span>;
      case 'delivered': return <span className="badge badge-success">已送达</span>;
      case 'cancelled': return <span className="badge badge-danger">已取消</span>;
      default: return <span className="badge badge-neutral">{status}</span>;
    }
  };

  return (
    <div className="flex-col gap-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-3">
          <Receipt size={28} className="text-primary" /> 
          资金与订单管理
        </h1>
        <button className="glass-button secondary" onClick={fetchOrders}>
          <RefreshCw size={18} className="text-primary" /> 刷新流水
        </button>
      </div>

      <div className="glass-panel p-6 flex flex-wrap gap-4 items-center mb-6">
        <div className="flex-1 relative">
          <div style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }}>
            <Search size={18} />
          </div>
          <input
            type="text"
            placeholder="搜索订单号/商家名/手机号..."
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
          <option value="pending">待支付</option>
          <option value="paid">已支付</option>
          <option value="delivering">配送中</option>
          <option value="delivered">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
      </div>

      <div className="glass-panel glass-table-container">
        {loading ? (
          <div className="p-8 text-center text-muted">加载中...</div>
        ) : (
          <table className="glass-table">
            <thead>
              <tr>
                <th>订单信息</th>
                <th>买卖双方</th>
                <th>商品详情</th>
                <th>总金额</th>
                <th>状态</th>
                <th className="text-right">操作干预</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>
                    <div className="font-semibold text-sm mb-1">{order.orderNo}</div>
                    <div className="text-xs text-secondary">
                      {new Date(order.createdAt).toLocaleString()}
                    </div>
                  </td>
                  <td>
                    <div className="text-sm mb-1">买家: <span className="font-medium text-primary">{order.user?.nickname}</span> ({order.user?.phone})</div>
                    <div className="text-sm">卖家: <span className="font-medium text-secondary">{order.merchantName || '个人卖家'}</span></div>
                  </td>
                  <td>
                    <div className="flex-col gap-1 text-xs text-secondary">
                      {order.items?.slice(0, 2).map((item, idx) => (
                        <div key={idx}>{item.name} <span className="font-semibold text-primary">x{item.quantity}</span></div>
                      ))}
                      {order.items?.length > 2 && <div className="text-muted">...等{order.items.length}件商品</div>}
                    </div>
                  </td>
                  <td>
                    <div className="font-bold text-danger text-lg">¥{order.totalAmount.toFixed(2)}</div>
                  </td>
                  <td>
                    {getStatusBadge(order.status)}
                  </td>
                  <td className="text-right">
                    <div className="flex flex-wrap gap-2 justify-end">
                      
                      {/* Can assign rider if paid/preparing */}
                      {['paid', 'preparing'].includes(order.status) && (
                        <button className="glass-button secondary p-2 px-3 text-sm" onClick={() => handleAction(order.id, 'assign_rider', '确认指派虚拟骑手配送？')}>
                          <Send size={16} className="text-info" /> 指派骑手
                        </button>
                      )}
                      
                      {/* Can complete if delivering */}
                      {['delivering'].includes(order.status) && (
                        <button className="glass-button success p-2 px-3 text-sm" onClick={() => handleAction(order.id, 'complete', '确认强制完成订单并进行资金结算？')}>
                          <CheckCircle size={16} /> 强制完成
                        </button>
                      )}

                      {/* Can refund if not delivered/cancelled */}
                      {!['delivered', 'cancelled'].includes(order.status) && (
                        <button className="glass-button danger p-2 px-3 text-sm" onClick={() => handleAction(order.id, 'refund', '确认强制取消并退款？')}>
                          <XCircle size={16} /> 强制退款
                        </button>
                      )}

                    </div>
                  </td>
                </tr>
              ))}
              {orders.length === 0 && (
                <tr>
                  <td colSpan={6} className="p-8 text-center text-muted">暂无订单</td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
