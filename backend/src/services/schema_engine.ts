export interface DynamicField {
  key: string;
  label: string;
  fieldType: 'TEXT' | 'SELECT' | 'MULTI_SELECT' | 'NUMBER' | 'BOOLEAN';
  required: boolean;
  options?: string[];
  placeholder?: string;
}

const schemas: Record<string, DynamicField[]> = {
  // 5. 房屋出租 (Rent) - 租房专属表单
  cat_5_rent: [
    { key: 'price_per_month', label: '租金/月(元)', fieldType: 'NUMBER', required: true, placeholder: '例: 1500' },
    { key: 'payment_method', label: '付款方式', fieldType: 'SELECT', required: true, options: ['押一付三', '押一付一', '年付', '面议'] },
    { key: 'facilities', label: '配置设施', fieldType: 'MULTI_SELECT', required: false, options: ['床', '宽带', '电视', '冰箱', '洗衣机', '空调', '独立卫生间', '阳台'] },
    { key: 'layout', label: '户型', fieldType: 'SELECT', required: true, options: ['1室1厅1卫', '2室1厅1卫', '3室2厅2卫', '4室及以上', '单间', '床位'] },
    { key: 'area', label: '面积(平方米)', fieldType: 'NUMBER', required: true, placeholder: '例: 80' }
  ],
  // 6. 二手房产 (Sale) - 售房专属表单
  cat_6_sale: [
    { key: 'total_price', label: '总价(万元)', fieldType: 'NUMBER', required: true, placeholder: '例: 120' },
    { key: 'down_payment', label: '首付(万元)', fieldType: 'NUMBER', required: true, placeholder: '例: 36' },
    { key: 'property_years', label: '产权年限', fieldType: 'SELECT', required: true, options: ['70年', '50年', '40年', '其他'] },
    { key: 'five_years_unique', label: '满五唯一', fieldType: 'SELECT', required: true, options: ['是', '否'] },
    { key: 'layout', label: '户型', fieldType: 'SELECT', required: true, options: ['1室1厅1卫', '2室1厅1卫', '3室2厅2卫', '4室及以上', '其他'] },
    { key: 'area', label: '面积(平方米)', fieldType: 'NUMBER', required: true, placeholder: '例: 120' }
  ],
  // 1. 个人闲置
  cat_1_idle: [
    { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新', '99新', '95新', '9成新', '8成新及以下'] },
    { key: 'delivery', label: '交易方式', fieldType: 'SELECT', required: true, options: ['自提', '同城面交', '邮寄'] }
  ],
  // 2. 家政/护理
  cat_2_service: [
    { key: 'service_type', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务', '门店服务', '电话咨询'] },
    { key: 'billing', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按次', '按小时', '按月', '面议'] }
  ],
  // 3. 便民维修
  cat_3_repair: [
    { key: 'repair_type', label: '维修范围', fieldType: 'TEXT', required: true, placeholder: '例: 上门修水管、开锁' },
    { key: 'fee', label: '上门费', fieldType: 'SELECT', required: true, options: ['免费上门', '收费上门(修好抵扣)', '固定上门费'] }
  ],
  // 8. 招聘求职
  cat_8_job: [
    { key: 'salary', label: '薪资待遇', fieldType: 'SELECT', required: true, options: ['面议', '3000以下', '3000-5000', '5000-8000', '8000-12000', '12000以上'] },
    { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1年以内', '1-3年', '3-5年', '5年以上'] },
    { key: 'education', label: '学历要求', fieldType: 'SELECT', required: true, options: ['不限', '初中及以下', '高中/中专', '大专', '本科及以上'] }
  ],
  // 7. 拼车/出行
  cat_7_carpool: [
    { key: 'departure', label: '出发地', fieldType: 'TEXT', required: true, placeholder: '例: 清远市城' },
    { key: 'destination', label: '目的地', fieldType: 'TEXT', required: true, placeholder: '例: 清远市区' },
    { key: 'seats', label: '提供/需要座位数', fieldType: 'NUMBER', required: true, placeholder: '例: 3' },
    { key: 'time', label: '出发时间', fieldType: 'TEXT', required: true, placeholder: '例: 8月10日 上午9点' }
  ],
  // Fallback default
  default: []
};

export class SchemaEngine {
  /**
   * Dynamically return schema based on category ID.
   * This decouples the schema structure from the static database seed.
   */
  static getSchema(categoryId: string): DynamicField[] {
    if (!categoryId) return schemas['default'];
    
    // Exact match
    if (schemas[categoryId]) {
      return schemas[categoryId];
    }
    
    // Prefix match (e.g. cat_5_rent_share falls back to cat_5_rent)
    for (const key of Object.keys(schemas)) {
      if (key !== 'default' && categoryId.startsWith(key)) {
        return schemas[key];
      }
    }
    
    return schemas['default'];
  }
}
