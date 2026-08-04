import 'dotenv/config';
import { PrismaClient } from '@prisma/client';
// 自包含的商家种子数据 (后端独立可部署)
import { merchantsData } from './seed-data.js';

const prisma = new PrismaClient();

const categoryTreeSeed = [
  // 1. 个人闲置（全栈升级：8大二级大类及三级叶子分类）
  {
    id: 'cat_idle',
    name: '个人闲置',
    icon: 'shopping-bag',
    iconUrl: '/assets/icons/3d_flat_secondhand.png',
    sortOrder: 1,
    isLeaf: false,
    isActive: true,
    isHot: true,
    children: [
      // 1.1 数码 3C
      {
        id: 'cat_3c',
        name: '数码 3C',
        iconUrl: '/assets/icons/3d_flat_digital.png',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'second_hand',
            name: '手机/平板',
            icon: '📱',
            iconUrl: '/assets/icons/3d_flat_digital.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'brand', label: '品牌', fieldType: 'SELECT', required: true, options: ['Apple/苹果', '小米', '华为', '荣耀', 'OPPO', 'vivo', '三星', '联想/荣耀平板', '其他'] },
              { key: 'model', label: '具体型号', fieldType: 'TEXT', required: true, placeholder: '例: iPhone 15 Pro / iPad Air 5 / 小米14' },
              { key: 'storage', label: '存储容量', fieldType: 'SELECT', required: true, options: ['64GB', '128GB', '256GB', '512GB', '1TB及以上'] },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新未拆', '99新充新', '95新轻微痕迹', '9成新', '8成新及以下'] },
            ]),
          },
          {
            id: 'cat_3c_pc',
            iconUrl: '/assets/icons/3d_flat_digital.png',
            name: '电脑/配件',
            icon: '💻',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '硬件类型', fieldType: 'SELECT', required: true, options: ['笔记本电脑', '台式机/整机', '显卡/显卡拓展', 'CPU/主板/内存/硬盘', '显示器/键盘/鼠标/外设'] },
              { key: 'brand', label: '品牌型号', fieldType: 'TEXT', required: true, placeholder: '例: 联想ThinkPad / MacBook Pro / RTX 4060 / AOC显示器' },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新', '99新充新', '95新正常使用', '9成新', '8成新及以下'] },
            ]),
          },
          {
            id: 'cat_3c_camera',
            iconUrl: '/assets/icons/3d_flat_digital.png',
            name: '摄影/无人机',
            icon: '📷',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'brand', label: '品牌', fieldType: 'SELECT', required: true, options: ['索尼 Sony', '佳能 Canon', '尼康 Nikon', '富士 Fujifilm', '大疆 DJI', '理光 / 徕卡 / 其他'] },
              { key: 'type', label: '器械品类', fieldType: 'SELECT', required: true, options: ['微单相机/单反机身', '相机镜头/变焦定焦', '无人机/航拍器', '运动相机/GoPro/Osmo', '三脚架/闪光灯等摄影配件'] },
              { key: 'condition', label: '成色与快门数', fieldType: 'SELECT', required: true, options: ['全新箱全', '99新仅拆快门极少', '95新无无霉无雾', '9成新有正常磨损'] },
            ]),
          },
          {
            id: 'cat_3c_audio',
            iconUrl: '/assets/icons/3d_flat_digital.png',
            name: '影音智能',
            icon: '🎧',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '智能设备品类', fieldType: 'SELECT', required: true, options: ['蓝牙耳机/AirPods/头戴式耳机', '智能手表/运动手环/Apple Watch', '蓝牙音箱/智能音箱/HomePod', '路由器/NAS网络存储/投影仪', 'VR/AR头显眼镜'] },
              { key: 'brand', label: '品牌名称', fieldType: 'TEXT', required: false, placeholder: '例: Apple / 索尼 / 华为 / 哈曼卡顿 / 小米' },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新未拆封', '99新箱全', '95新功能完好', '9成新及以下'] },
            ]),
          },
        ],
      },
      // 1.2 服饰箱包
      {
        id: 'cat_clothing',
        name: '服饰箱包',
        iconUrl: '/assets/icons/3d_flat_clothing.png',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_dress',
            name: '服装、皮鞋',
            icon: '👗',
            iconUrl: '/assets/icons/3d_flat_clothing.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'gender', label: '适用对象', fieldType: 'SELECT', required: true, options: ['时尚女装 (外套/连衣裙/上衣/裤装)', '潮流男装 (夹克/卫衣/T恤/休闲裤/西装)', '情侣装/中性潮流服饰'] },
              { key: 'size', label: '尺码', fieldType: 'SELECT', required: true, options: ['XS', 'S', 'M', 'L', 'XL', '2XL', '3XL及以上', '均码', '鞋码35-45'] },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新带吊牌/水洗未穿', '99新仅试穿/洗涤一次', '9成新无污渍破损', '8成新及以下'] },
            ]),
          },
          {
            id: 'cat_bag',
            iconUrl: '/assets/icons/3d_flat_clothing.png',
            name: '箱包/皮具',
            icon: '👜',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '箱包款式', fieldType: 'SELECT', required: true, options: ['双肩背包/电脑包', '单肩斜挎包/链条包', '手提包/托特包/公文包', '旅行箱/拉杆箱(20寸/24寸/28寸)', '零钱包/卡包/腰包'] },
              { key: 'brand', label: '品牌', fieldType: 'TEXT', required: false, placeholder: '例: 小米 / 新秀丽 / Coach / 蔻驰 / MK / 蔻驰' },
              { key: 'condition', label: '五金与皮质成色', fieldType: 'SELECT', required: true, options: ['全新带吊牌', '99新无磨损完美', '95新轻微正常使用痕迹', '9成新及以下'] },
            ]),
          },
          {
            id: 'cat_luxury',
            iconUrl: '/assets/icons/3d_flat_clothing.png',
            name: '配饰/腕表',
            icon: '⌚',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '饰品分类', fieldType: 'SELECT', required: true, options: ['手表/机械表/石英表', '项链/吊坠/手链/手镯', '戒指/耳环/首饰盒', '太阳镜/墨镜/近视镜框', '帽子/围巾/皮带配饰'] },
              { key: 'source', label: '来源及凭证', fieldType: 'SELECT', required: true, options: ['专柜购买(发票保卡齐全)', '国内平台购买有订单凭证', '免税店/海外直邮凭证', '闲置转让/礼品无需凭证'] },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新未戴', '99新全套靓品', '95新微痕', '9成新'] },
            ]),
          },
        ],
      },
      // 1.3 日用/家电
      {
        id: 'cat_home',
        name: '日用/家电',
        iconUrl: '/assets/icons/3d_flat_appliance.png',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_home_appliance',
            iconUrl: '/assets/icons/3d_flat_appliance.png',
            name: '家用电器',
            icon: '📺',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '家电品类', fieldType: 'SELECT', required: true, options: ['冰箱/冰柜/冷饮机', '洗衣机/烘干机/洗烘套装', '液晶电视/投影仪/回音壁', '空调(挂机/立柜式/移动空调)', '微波炉/烤箱/空气炸锅/咖啡机', '吸尘器/扫地机器人/空气净化器'] },
              { key: 'brand', label: '品牌型号', fieldType: 'TEXT', required: true, placeholder: '例: 美的 / 格力 / 海尔 / 小米 / 西门子' },
              { key: 'condition', label: '年限与运行状态', fieldType: 'SELECT', required: true, options: ['全新未安装(带保修)', '使用1年内(功能完美在保)', '使用1-3年正常无修', '使用3年以上正常运转'] },
              { key: 'delivery', label: '交接提货', fieldType: 'SELECT', required: true, options: ['买家同城上门自提', '卖家包送或协助找货运拉车'] },
            ]),
          },
          {
            id: 'cat_home_furniture',
            iconUrl: '/assets/icons/3d_flat_appliance.png',
            name: '家具/家居',
            icon: '🛋️',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '家具款式', fieldType: 'SELECT', required: true, options: ['床/床垫/榻榻米', '沙发(布艺/真皮/科技布)/茶几', '餐桌/书桌/电脑桌/电竞椅/办公椅', '衣柜/书柜/鞋柜/储物置物架'] },
              { key: 'material', label: '主要材质', fieldType: 'SELECT', required: false, options: ['实木/整木', '布艺/绒面', '头层真皮/仿皮', '优质板式/密度板', '金属/铁艺/钢化玻璃'] },
              { key: 'delivery', label: '搬运提示', fieldType: 'SELECT', required: true, options: ['同城买家自提(需自行拆卸搬运)', '买家自提(卖家协助拆卸电梯房方便)', '卖家协商包货运包送'] },
            ]),
          },
          {
            id: 'cat_home_daily',
            name: '日用杂货',
            icon: '🧹',
            iconUrl: '/assets/icons/3d_flat_appliance.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '日杂品类', fieldType: 'SELECT', required: true, options: ['厨具餐具/锅具刀具/水杯茶具', '收纳整理箱/衣架/储物盒', '床上四件套/夏凉被/枕头毛毯', '灯具/装饰摆件/绿植花盆/挂画', '闲置节庆礼品/家庭日杂小件'] },
              { key: 'condition', label: '成色与卫生', fieldType: 'SELECT', required: true, options: ['全新带原包装/未洗未使用', '99新仅拆封清洗', '9成新干净好用'] },
            ]),
          },
        ],
      },
      // 1.4 美妆个护
      {
        id: 'cat_beauty',
        name: '美妆个护',
        iconUrl: '/assets/icons/3d_flat_beauty.png',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        isHot: false,
        children: [
          {
            id: 'cat_beauty_skin',
            name: '护肤/彩妆',
            icon: '💄',
            iconUrl: '/assets/icons/3d_flat_beauty.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '美妆护肤品类', fieldType: 'SELECT', required: true, options: ['面部精华/乳液/面霜/眼霜', '爽肤水/精粹水/防晒隔离', '口红/唇釉/润唇膏', '粉底液/气垫/遮瑕/定妆散粉', '眼影盘/腮红/高光修容盘', '护肤彩妆精美礼盒套装'] },
              { key: 'brand', label: '品牌名称', fieldType: 'TEXT', required: true, placeholder: '例: 雅诗兰黛 / 兰蔻 / 海蓝之谜 / YSL / 珀莱雅' },
              { key: 'expiry', label: '保质期与余量', fieldType: 'SELECT', required: true, options: ['全新未拆封(保质期1年以上)', '全新未拆封(保质期1年内近期无过期)', '仅手背试色/喷试(余量99%)', '已拆封使用(余量80%以上卫生完好)'] },
            ]),
          },
          {
            id: 'cat_beauty_care',
            name: '香水/洗护',
            icon: '🧴',
            iconUrl: '/assets/icons/3d_flat_beauty.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '洗护与仪器品类', fieldType: 'SELECT', required: true, options: ['大牌香水/淡香水/身体香氛喷雾', '洗发水/护发素/精油/身体乳', '戴森吹风机/卷发棒/直发梳', '美容仪/射频导入仪/洁面仪', '化妆刷具套装/收纳包/日用个护工具'] },
              { key: 'condition', label: '状态说明', fieldType: 'SELECT', required: true, options: ['全新未拆未用(全套在盒)', '99新仅试用1-2次无划痕', '95新日常自用功能完美'] },
            ]),
          },
        ],
      },
      // 1.5 母婴儿童
      {
        id: 'cat_baby',
        name: '母婴儿童',
        iconUrl: '/assets/icons/3d_flat_maternal.png',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_baby_clothes',
            name: '童装/童鞋',
            icon: '🍼',
            iconUrl: '/assets/icons/3d_flat_maternal.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'age', label: '适合年龄段', fieldType: 'SELECT', required: true, options: ['0-1岁婴儿宝宝', '1-3岁幼儿', '3-6岁学龄前小童', '6-12岁大童'] },
              { key: 'type', label: '款式类别', fieldType: 'SELECT', required: true, options: ['童装连体衣/爬服套装', '外套/棉服/羽绒服', '学步鞋/儿童运动鞋/凉鞋', '配饰/睡袋/围巾帽子'] },
              { key: 'condition', label: '卫生与成色', fieldType: 'SELECT', required: true, options: ['全新带吊牌/下水洗净未穿', '99新穿过1-2次极新', '9成新干净整洁无污渍无破损'] },
            ]),
          },
          {
            id: 'cat_baby_stroller',
            name: '婴儿车/床',
            icon: '🚼',
            iconUrl: '/assets/icons/3d_flat_maternal.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '大件母婴品类', fieldType: 'SELECT', required: true, options: ['婴儿推车/高景观车/遛娃神车/折叠轻便车', '儿童汽车安全座椅(3C欧盟认证)', '实木婴儿床/拼接床/便携折叠床', '婴儿餐椅/摇摇椅/学步车'] },
              { key: 'brand', label: '品牌', fieldType: 'TEXT', required: false, placeholder: '例: 好孩子 / 宝得适 Britax / Stokke / 巧儿宜 / 康贝' },
              { key: 'condition', label: '使用成色', fieldType: 'SELECT', required: true, options: ['全新箱全', '95新极干净无破损件全', '9成新正常痕迹件完好', '8成新及以下'] },
              { key: 'delivery', label: '交易方式', fieldType: 'SELECT', required: true, options: ['同城上门看货自提', '协商车送或快递寄送'] },
            ]),
          },
          {
            id: 'cat_baby_toy',
            iconUrl: '/assets/icons/3d_flat_maternal.png',
            name: '儿童玩具/绘本',
            icon: '🧸',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '玩具绘本品类', fieldType: 'SELECT', required: true, options: ['乐高拼装积木/益智拼图/拼装玩具', '中外经典儿童绘本/分级阅读英语/立体书', '儿童电动四轮车/滑板车/无脚踏平衡车', '点读笔(毛毛虫/小达人等)/早教故事机', '毛绒公仔/安抚玩具/过家家玩具组'] },
              { key: 'condition', label: '完整度与成色', fieldType: 'SELECT', required: true, options: ['全新未拆盒装', '99新全套无缺件无涂画', '9成新有正常玩耍翻书痕迹'] },
            ]),
          },
        ],
      },
      // 1.6 运动 & 交通工具
      {
        id: 'cat_sports',
        name: '运动 & 交通工具',
        iconUrl: '/assets/icons/3d_flat_sports.png',
        sortOrder: 6,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_sports_bike',
            name: '自行车/电动车',
            icon: '🚴',
            iconUrl: '/assets/icons/3d_flat_sports.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '车辆品类 (同城极高频需求)', fieldType: 'SELECT', required: true, options: ['两轮电动车/电瓶车 (雅迪/九号/小牛/爱玛等)', '山地自行车/公路车/折叠变速车 (捷安特/美利达/大行等)', '电动滑板车/平衡车/代驾折叠车', '三轮电动车/老年代步车'] },
              { key: 'brandModel', label: '品牌与型号', fieldType: 'TEXT', required: true, placeholder: '例: 九号F90M / 捷安特ATX777 / 雅迪冠能 / 小牛NQI' },
              { key: 'batteryInfo', label: '电池与续航 (电动车必填)', fieldType: 'SELECT', required: false, options: ['原装锂电池(续航60km以上)', '原装铅酸电池(续航40-60km)', '近期更换新电池(动力强劲)', '人力自行车无需电池'] },
              { key: 'licenseInfo', label: '上牌与证照情况', fieldType: 'SELECT', required: true, options: ['有正规收据发票/已上同城合法白牌或绿牌可过户', '有发票合格证/未上牌可直接上牌', '闲置车转让验车无误当面交接'] },
              { key: 'condition', label: '车况与成色', fieldType: 'SELECT', required: true, options: ['全新充新车99新', '95新车况优秀刹车轮胎完美', '9成新正常实用无大修无水泡', '8成新实用代步车'] },
            ]),
          },
          {
            id: 'cat_sports_gym',
            name: '运动健身',
            icon: '🏋️',
            iconUrl: '/assets/icons/3d_flat_sports.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '器材运动品类', fieldType: 'SELECT', required: true, options: ['跑步机/椭圆机/划船机/动感单车', '哑铃/杠铃/杠铃凳/壶铃/拉力器', '羽毛球拍/网球拍/乒乓球拍/高尔夫球杆', '篮球/足球/排球/滑板/轮滑鞋', '瑜伽垫/泡沫轴/健身护具衣物'] },
              { key: 'condition', label: '器械成色', fieldType: 'SELECT', required: true, options: ['全新未拆', '95新使用正常无故障', '9成新实用好用'] },
              { key: 'delivery', label: '交接方式', fieldType: 'SELECT', required: true, options: ['重型健身器械需买家上门自提', '同城轻物可面交或快递', '卖家协商包运送'] },
            ]),
          },
          {
            id: 'cat_sports_camp',
            name: '户外露营',
            icon: '⛺',
            iconUrl: '/assets/icons/3d_flat_sports.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '户外装备品类', fieldType: 'SELECT', required: true, options: ['帐篷/天幕/露营推车营地车', '户外折叠椅/蛋卷桌/睡袋/防潮垫', '台钓竿/路亚竿/溪流竿/渔轮/钓箱渔具', '卡式炉/户外气炉/钛杯茶具餐具', '滑雪板/滑雪服/雪镜登山杖背包'] },
              { key: 'brand', label: '品牌名称', fieldType: 'TEXT', required: false, placeholder: '例: 挪客 Naturehike / 牧高笛 / 探险者 / 光威 / 达瓦' },
              { key: 'condition', label: '成色与完整度', fieldType: 'SELECT', required: true, options: ['全新未拆', '99新仅户外使用一次无破损无破洞', '95新成色良好', '9成新正常痕迹'] },
            ]),
          },
        ],
      },
      // 1.7 文娱爱好
      {
        id: 'cat_hobby',
        name: '文娱爱好',
        iconUrl: '/assets/icons/3d_flat_entertainment.png',
        sortOrder: 7,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_hobby_book',
            name: '图书/文具',
            icon: '📚',
            iconUrl: '/assets/icons/3d_flat_entertainment.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '图书文具类别', fieldType: 'SELECT', required: true, options: ['考研考公/法考/教资等职业资格复习资料教材', '大学各学科专业教材/参考书', '中外文学小说/散文/历史传记/心理管理书籍', '绝版连环画/经典漫画全集/杂志丛书', '钢笔/凌美/计算器等电子或文具用品'] },
              { key: 'condition', label: '书况与涂写情况', fieldType: 'SELECT', required: true, options: ['全新塑封/未折未写', '99新极干净无划线无笔记', '95新轻微翻阅无破损有少量铅笔勾画', '85新有正常笔记或重点划线(适合备考使用)'] },
            ]),
          },
          {
            id: 'cat_hobby_music',
            name: '乐器',
            icon: '🎸',
            iconUrl: '/assets/icons/3d_flat_entertainment.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '乐器或周边品类', fieldType: 'SELECT', required: true, options: ['木吉他/民谣吉他/电吉他/尤克里里', '电钢琴/电子琴/传统钢琴/合成器', '古筝/二胡/笛箫/小提琴等管弦乐器', '乐器音箱/吉他包/调音台乐谱等配件'] },
              { key: 'brand', label: '品牌名称', fieldType: 'TEXT', required: false, placeholder: '例: 雅马哈 / 卡西欧 / 芬达 / 罗兰 / 泰勒 / 星海' },
              { key: 'condition', label: '乐器成色与音准', fieldType: 'SELECT', required: true, options: ['全新未拆封', '99新琴体完美音准好音色靓', '95新轻微正常使用无磕碰', '9成新正常演奏'] },
            ]),
          },
          {
            id: 'cat_hobby_figure',
            name: '游戏/手办',
            icon: '🎮',
            iconUrl: '/assets/icons/3d_flat_entertainment.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '游戏与手办品类', fieldType: 'SELECT', required: true, options: ['Switch/PS5/SteamDeck等游戏主机器械', 'Switch/PS4/PS5游戏卡带与光盘', '泡泡玛特/盲盒/隐藏款手办摆件', '高达模型/动漫正比例手办/粘土人景品', '二次元谷子/徽章/亚克力/同人周边', '宝可梦/奥特曼卡牌/桌游棋牌'] },
              { key: 'version', label: '版本属性', fieldType: 'SELECT', required: true, options: ['国行/日版/港版官方正品(盒证齐全)', '官方正品(无盒仅本体)', '开盒未拆袋确认款(盲盒适用)', '高性价散货/高仿请如实声明'] },
              { key: 'condition', label: '成色说明', fieldType: 'SELECT', required: true, options: ['全新未拆封', '99新仅拆封试玩/拆摆好品无瑕疵', '95新无缺件功能正常', '微瑕/有缺件(请描述详情)'] },
            ]),
          },
          {
            id: 'cat_hobby_pet',
            name: '宠物用品',
            icon: '🐈',
            iconUrl: '/assets/icons/3d_flat_entertainment.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'notice', label: '特别提示', fieldType: 'SELECT', required: true, options: ['本分类仅限闲置用品及合封日粮转让(严禁发布活体猫犬交易避免纠纷)'] },
              { key: 'type', label: '宠物用品品类', fieldType: 'SELECT', required: true, options: ['猫砂盆/自动猫砂机/猫爬架/猫抓板/猫别墅', '狗笼/航空箱/外带便携包/宠物推车', '鱼缸/水族箱/加热棒/水泵过滤器造景', '自动喂食器/智能饮水机/宠物烘干箱/吹水机', '全新未拆封猫粮狗粮/罐头零食/常备品'] },
              { key: 'condition', label: '成色与清洁消毒说明', fieldType: 'SELECT', required: true, options: ['全新未拆封未使用', '99新使用极短已彻底清洗无异味消毒完好', '95新使用正常卫生良好'] },
            ]),
          },
          {
            id: 'cat_hobby_ticket',
            name: '演出/门票',
            icon: '🎫',
            iconUrl: '/assets/icons/3d_flat_entertainment.png',
            sortOrder: 5,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '门票类型', fieldType: 'SELECT', required: true, options: ['同城电影通兑券/选座代下', '演唱会/音乐会/话剧/脱口秀门票', '同城景区门票/游乐园/水上乐园/温泉门票', '漫展/车展/博览会通票'] },
              { key: 'dateInfo', label: '演出或使用具体日期时间', fieldType: 'TEXT', required: true, placeholder: '例: 2026-08-20 晚19:30 / 周末法定节假日通用' },
              { key: 'realName', label: '实名入场规则', fieldType: 'SELECT', required: true, options: ['非实名通用电子券/纸质票(直接出示转赠即可)', '实名转赠票(通过官方小程序转赠给买家身份证)', '需协助录入实名信息入场'] },
            ]),
          },
        ],
      },
      // 1.8 其它
      {
        id: 'cat_other',
        name: '其它',
        iconUrl: '/assets/icons/3d_flat_others.png',
        sortOrder: 8,
        isLeaf: false,
        isActive: true,
        isHot: false,
        children: [
          {
            id: 'cat_other_idle',
            name: '其他闲置 (包括服务)',
            icon: '📦',
            iconUrl: '/assets/icons/3d_flat_others.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'itemType', label: '物品/服务名称描述', fieldType: 'TEXT', required: true, placeholder: '例: 闲置工艺收藏摆件 / 冷门五金五金工具 / 闲置办公用品 / 技能服务' },
              { key: 'condition', label: '成色与状态说明', fieldType: 'SELECT', required: true, options: ['全新未拆未使用', '99新充新好品', '9成新正常功能完好', '8成新及以下实用级', '服务类无需成色'] },
              { key: 'tradeWay', label: '交接方式', fieldType: 'SELECT', required: true, options: ['同城当面交易面交自提', '快递寄送/运费协商', '双方线上/线下协商'] },
            ]),
          },
        ],
      },
    ],

  },

  // 2. 二手房
  {
    id: 'cat_house_sale',
    name: '二手房',
    icon: 'home',
    iconUrl: '/assets/icons/3d_flat_house_sale.png',
    sortOrder: 2,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'house_sale_residential',
        name: '普通住宅',
        icon: '🏢',
        iconUrl: '/assets/icons/3d_flat_house_sale.png',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'price', label: '售价', fieldType: 'SELECT', required: true, options: ['面议', '万元'] },
          { key: 'layout', label: '户型', fieldType: 'SELECT', required: true, options: ['1室1厅1卫', '2室1厅1卫', '3室2厅2卫', '4室及以上', '其他'] },
          { key: 'area', label: '面积(平方米)', fieldType: 'TEXT', required: true, placeholder: '例: 120' },
          { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['电梯房', '满五唯一', '带车位', '精装修', '近学校'] },
        ]),
      },
      {
        id: 'house_sale_villa',
        name: '别墅/排屋',
        icon: '🏡',
        iconUrl: '/assets/icons/3d_flat_house_sale.png',
        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'price', label: '售价(万元)', fieldType: 'TEXT', required: true, placeholder: '例: 500' },
          { key: 'area', label: '建筑面积', fieldType: 'TEXT', required: true, placeholder: '例: 300' },
          { key: 'facilities', label: '特色', fieldType: 'MULTI_SELECT', required: false, options: ['带院子', '带地下室', '精装修', '双车位'] },
        ]),
      },
    ],
  },
  // 3. 租房
  {
    id: 'cat_house_rent',
    name: '租房',
    icon: 'home',
    iconUrl: '/assets/icons/3d_flat_house_rent.png',
    sortOrder: 3,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'house_rent_full',
            name: '整套出租',
            icon: '🏠',
            iconUrl: '/assets/icons/3d_flat_house_rent.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金', fieldType: 'SELECT', required: true, options: ['面议', '元/月', '万元'] },
              { key: 'layout', label: '户型', fieldType: 'SELECT', required: true, options: ['1室1厅1卫', '2室1厅1卫', '3室2厅2卫', '4室及以上', '其他'] },
              { key: 'area', label: '面积(平方米)', fieldType: 'TEXT', required: true, placeholder: '例: 80' },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['床', '宽带', '电视', '冰箱', '独立卫生间', '洗衣机', '空调', '阳台'] },
            ]),
          },
          {
            id: 'house_rent_share',
            name: '单间合租',
            icon: '🚪',
            iconUrl: '/assets/icons/3d_flat_house_rent.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金', fieldType: 'SELECT', required: true, options: ['面议', '元/月'] },
              { key: 'area', label: '房间面积', fieldType: 'TEXT', required: true, placeholder: '例: 15' },
              { key: 'facilities', label: '房间配套', fieldType: 'MULTI_SELECT', required: false, options: ['床', '衣柜', '书桌', '空调', '独立卫浴', '飘窗'] },
            ]),
          },
          {
            id: 'house_rent_bed',
            name: '床位出租',
            icon: '🛏️',
            iconUrl: '/assets/icons/3d_flat_house_rent.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金', fieldType: 'SELECT', required: true, options: ['面议', '元/月'] },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['储物柜', '空调', '独立插座', '宽带'] },
            ]),
          },
        ],
      },

  // 3. 家政保洁
  {
    id: 'cat_service',
    name: '家政保洁',
    icon: 'cleaning-services',
    iconUrl: '/assets/icons/3d_flat_cleaning.png',
    sortOrder: 3,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'cat_service_daily',
            iconUrl: '/assets/icons/3d_flat_service_daily.png',
        name: '家庭保洁',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_daily_normal',
            name: '日常保洁',
            icon: '🧹',
            iconUrl: '/assets/icons/3d_flat_service_daily.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按小时', '按次', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['实名认证', '爽约包赔', '自带工具'] },
            ]),
          },
          {
            id: 'service_daily_deep',
            name: '深度保洁',
            icon: '🧽',
            iconUrl: '/assets/icons/3d_flat_service_daily.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按平米', '按次', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['高温杀菌', '去除顽渍', '爽约包赔'] },
            ]),
          },
          {
            id: 'service_daily_glass',
            name: '擦玻璃',
            icon: '✨',
            iconUrl: '/assets/icons/3d_flat_service_daily.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按平米', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['专业工具', '双面擦拭', '安全保障'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_service_new',
            iconUrl: '/assets/icons/3d_flat_service_new.png',
        name: '开荒保洁',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_new_house',
            name: '新房开荒',
            icon: '🏡',
            iconUrl: '/assets/icons/3d_flat_service_new.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按平米', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['去除甲醛', '除胶除漆', '不满意重做'] },
            ]),
          },
          {
            id: 'service_new_rent',
            name: '出租房开荒',
            icon: '🏢',
            iconUrl: '/assets/icons/3d_flat_service_new.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按平米', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['全面消毒', '清理杂物', '爽约包赔'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_service_appliance',
            iconUrl: '/assets/icons/3d_flat_service_appliance.png',
        name: '家电清洗',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_appliance_ac',
            name: '空调清洗',
            icon: '❄️',
            iconUrl: '/assets/icons/3d_flat_service_appliance.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按台', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['深度拆洗', '高温消毒', '不满意重做'] },
            ]),
          },
          {
            id: 'service_appliance_hood',
            name: '油烟机清洗',
            icon: '🍳',
            iconUrl: '/assets/icons/3d_flat_service_appliance.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按台', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['重油污清除', '免拆洗/深度拆洗', '爽约包赔'] },
            ]),
          },
          {
            id: 'service_appliance_washer',
            name: '洗衣机清洗',
            icon: '👕',
            iconUrl: '/assets/icons/3d_flat_service_appliance.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按台', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['内筒消毒', '除垢除味', '不满意重做'] },
            ]),
          },
          {
            id: 'service_appliance_fridge',
            name: '冰箱清洗',
            icon: '🧊',
            iconUrl: '/assets/icons/3d_flat_service_appliance.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按台', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['除冰除味', '臭氧杀菌', '爽约包赔'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_service_nanny',
            iconUrl: '/assets/icons/3d_flat_service_nanny.png',
        name: '保姆/钟点',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_nanny_hourly',
            name: '钟点工保洁',
            icon: '⏱️',
            iconUrl: '/assets/icons/3d_flat_service_nanny.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按小时', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['实名认证', '健康证', '爽约包赔'] },
            ]),
          },
          {
            id: 'service_nanny_full',
            name: '全职保姆',
            icon: '👩‍🍳',
            iconUrl: '/assets/icons/3d_flat_service_nanny.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['住家保姆', '白班保姆'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按月', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['实名认证', '持证上岗', '包换服务'] },
            ]),
          },
          {
            id: 'service_nanny_cook',
            name: '做饭阿姨',
            icon: '🥘',
            iconUrl: '/assets/icons/3d_flat_service_nanny.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门做饭'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按次', '按月', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['实名认证', '健康证', '爽约包赔'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_service_maternity',
            iconUrl: '/assets/icons/3d_flat_service_maternity.png',
        name: '育儿/陪护',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_mat_yuesao',
            name: '月嫂',
            icon: '👶',
            iconUrl: '/assets/icons/3d_flat_service_maternity.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['住家服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按月(26天)', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['持母婴护理证', '金牌月嫂', '不满意重做'] },
            ]),
          },
          {
            id: 'service_mat_yuer',
            name: '育儿嫂',
            icon: '🍼',
            iconUrl: '/assets/icons/3d_flat_service_maternity.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['住家服务', '白班服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按月', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['持育婴师证', '早教经验', '实名认证'] },
            ]),
          },
          {
            id: 'service_mat_nurse',
            name: '医院陪护',
            icon: '🏥',
            iconUrl: '/assets/icons/3d_flat_service_maternity.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['医院陪护'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按天', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['持护工证', '专业护理', '实名认证'] },
            ]),
          },
          {
            id: 'service_mat_elderly',
            name: '居家养老陪护',
            icon: '👵',
            iconUrl: '/assets/icons/3d_flat_service_maternity.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['住家陪护', '白班陪护'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按月', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['经验丰富', '耐心负责', '实名认证'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_service_moving',
            iconUrl: '/assets/icons/3d_flat_service_moving.png',
        name: '搬家/货运',
        sortOrder: 6,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_moving_small',
            name: '小型搬家',
            icon: '📦',
            iconUrl: '/assets/icons/3d_flat_service_moving.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['同城搬家'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按次', '按距离', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['帮忙搬运', '不乱加价'] },
            ]),
          },
          {
            id: 'service_moving_family',
            name: '居民搬家',
            icon: '🚚',
            iconUrl: '/assets/icons/3d_flat_service_moving.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['同城搬家', '跨城搬家'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按车', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['专业团队', '打包服务', '家具拆装'] },
            ]),
          },
          {
            id: 'service_moving_freight',
            name: '货运拉货',
            icon: '🚛',
            iconUrl: '/assets/icons/3d_flat_service_moving.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['同城拉货', '跨城拉货'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按次', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['多种车型', '按时送达'] },
            ]),
          },
          {
            id: 'service_moving_equip',
            name: '设备搬迁',
            icon: '🏗️',
            iconUrl: '/assets/icons/3d_flat_service_moving.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['公司搬迁', '工厂搬迁'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['专业设备', '安全保障', '开具发票'] },
            ]),
          }
        ]
      },
    ],
  },
  // 4. 水电维修
  {
    id: 'cat_maintenance',
    name: '水电维修',
    icon: 'build',
    iconUrl: '/assets/icons/3d_flat_repair.png',
    sortOrder: 4,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'cat_main_lock',
            iconUrl: '/assets/icons/3d_flat_main_lock.png',
        name: '开锁/换锁',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'main_lock_open',
            name: '防盗门开锁',
            icon: '🚪',
            iconUrl: '/assets/icons/3d_flat_main_lock.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_lock_change',
            name: '换锁芯/指纹锁',
            icon: '🔐',
            iconUrl: '/assets/icons/3d_flat_main_lock.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_lock_car',
            name: '汽车开锁',
            icon: '🚗',
            iconUrl: '/assets/icons/3d_flat_main_lock.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_main_pipe',
            iconUrl: '/assets/icons/3d_flat_main_pipe.png',
        name: '管道疏通',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'main_pipe_toilet',
            name: '马桶疏通',
            icon: '🚽',
            iconUrl: '/assets/icons/3d_flat_main_pipe.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1个月', '3个月'] },
            ]),
          },
          {
            id: 'main_pipe_kitchen',
            name: '厨房下水疏通',
            icon: '🚰',
            iconUrl: '/assets/icons/3d_flat_main_pipe.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1个月', '3个月'] },
            ]),
          },
          {
            id: 'main_pipe_sewer',
            name: '主管道/化粪池',
            icon: '🕳️',
            iconUrl: '/assets/icons/3d_flat_main_pipe.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1个月', '3个月'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_main_electrical',
            iconUrl: '/assets/icons/3d_flat_main_electrical.png',
        name: '水电维修',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'main_electrical_circuit',
            name: '电路维修',
            icon: '⚡',
            iconUrl: '/assets/icons/3d_flat_main_electrical.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_electrical_lamp',
            name: '灯具安装/维修',
            icon: '💡',
            iconUrl: '/assets/icons/3d_flat_main_electrical.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_electrical_water',
            name: '水管维修/安装',
            icon: '🚿',
            iconUrl: '/assets/icons/3d_flat_main_electrical.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_main_appliance',
            iconUrl: '/assets/icons/3d_flat_main_appliance.png',
        name: '家电维修',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'main_appliance_ac',
            name: '空调维修/加氟',
            icon: '❄️',
            iconUrl: '/assets/icons/3d_flat_main_appliance.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_appliance_fridge',
            name: '冰箱维修',
            icon: '🧊',
            iconUrl: '/assets/icons/3d_flat_main_appliance.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_appliance_washer',
            name: '洗衣机维修',
            icon: '👕',
            iconUrl: '/assets/icons/3d_flat_main_appliance.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_appliance_tv',
            name: '电视/影音维修',
            icon: '📺',
            iconUrl: '/assets/icons/3d_flat_main_appliance.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_main_waterproof',
            iconUrl: '/assets/icons/3d_flat_main_waterproof.png',
        name: '房屋修缮',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'main_waterproof_roof',
            name: '楼顶/外墙防水',
            icon: '🧱',
            iconUrl: '/assets/icons/3d_flat_main_waterproof.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1年', '3年', '5年及以上'] },
            ]),
          },
          {
            id: 'main_waterproof_bath',
            name: '卫生间/阳台漏水',
            icon: '🚿',
            iconUrl: '/assets/icons/3d_flat_main_waterproof.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1年', '3年', '5年及以上'] },
            ]),
          },
          {
            id: 'main_waterproof_paint',
            name: '粉刷/泥瓦/修补',
            icon: '🖌️',
            iconUrl: '/assets/icons/3d_flat_main_waterproof.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1年', '3年', '5年及以上'] },
            ]),
          }
        ]
      },
    ],
  },
  // 5. 同城生鲜
  {
    id: 'cat_veggies',
    name: '同城生鲜',
    icon: 'shopping-basket',
    iconUrl: '/assets/icons/3d_flat_fresh_food.png',
    sortOrder: 5,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'cat_veggies_fruit',
        name: '新鲜水果',
        icon: '🍎',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_veg',
        name: '时令蔬菜',
        icon: '🥬',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_meat',
        name: '肉禽蛋品',
        icon: '🥩',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 3,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'type', label: '种类', fieldType: 'SELECT', required: true, options: ['猪牛羊肉', '鸡鸭禽类', '蛋类', '其他'] },
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_seafood',
        name: '海鲜水产',
        icon: '🦞',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 4,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'condition', label: '状态', fieldType: 'SELECT', required: true, options: ['活鲜', '冰鲜', '干货', '冷冻'] },
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_frozen',
        name: '冷藏冻货',
        icon: '🧊',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 5,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'type', label: '种类', fieldType: 'SELECT', required: true, options: ['速冻面点', '冻肉冻品', '火锅丸子', '冷饮冰品', '其他'] },
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_oil',
        name: '粮油调味',
        icon: '🛢️',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 6,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
      {
        id: 'cat_veggies_deli',
        name: '熟食卤味',
        icon: '🍗',
        iconUrl: '/assets/icons/3d_flat_fresh_food.png',
        sortOrder: 7,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
          { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
          { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
        ]),
      },
    ],
  },
  // 6. 招聘求职
  {
    id: 'cat_job',
    name: '求职招聘',
    icon: 'work',
    iconUrl: '/assets/icons/3d_flat_jobs.png',
    sortOrder: 6,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'cat_job_hospitality',
        name: '餐饮/酒店',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_job_hospitality.png',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_hosp_waiter',
            name: '服务员',
            icon: '🍽️',
            iconUrl: '/assets/icons/3d_flat_job_hospitality.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '周末双休'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_hosp_chef',
            name: '厨师/后厨',
            icon: '👨‍🍳',
            iconUrl: '/assets/icons/3d_flat_job_hospitality.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '周末双休'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_hosp_hotel',
            name: '酒店前台/客房',
            icon: '🏨',
            iconUrl: '/assets/icons/3d_flat_job_hospitality.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '周末双休'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_hosp_guide',
            name: '导游/计调',
            icon: '🚩',
            iconUrl: '/assets/icons/3d_flat_job_hospitality.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '周末双休'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_job_blue_collar',
        name: '普工/制造',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_job_blue_collar.png',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_blue_worker',
            name: '普工/操作工',
            icon: '👷',
            iconUrl: '/assets/icons/3d_flat_job_blue_collar.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '加班补助'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_blue_tech',
            name: '技工(电工/焊工等)',
            icon: '🔧',
            iconUrl: '/assets/icons/3d_flat_job_blue_collar.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '加班补助'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_blue_manage',
            name: '车间管理',
            icon: '📋',
            iconUrl: '/assets/icons/3d_flat_job_blue_collar.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '加班补助'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_job_sales',
        name: '销售/客服',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_job_sales.png',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_sales_rep',
            name: '销售专员',
            icon: '💼',
            iconUrl: '/assets/icons/3d_flat_job_sales.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '底薪+提成', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_sales_cs',
            name: '客服专员',
            icon: '🎧',
            iconUrl: '/assets/icons/3d_flat_job_sales.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '底薪+提成', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_sales_marketing',
            name: '市场拓展/运营',
            icon: '📈',
            iconUrl: '/assets/icons/3d_flat_job_sales.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '底薪+提成', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_job_admin',
        name: '人事/财务',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_job_admin.png',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_admin_hr',
            name: '人事专员/助理',
            icon: '📝',
            iconUrl: '/assets/icons/3d_flat_job_admin.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          },
          {
            id: 'job_admin_frontdesk',
            name: '行政/前台',
            icon: '🗂️',
            iconUrl: '/assets/icons/3d_flat_job_admin.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          },
          {
            id: 'job_admin_finance',
            name: '财务/出纳/会计',
            icon: '💰',
            iconUrl: '/assets/icons/3d_flat_job_admin.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          },
          {
            id: 'job_admin_manager',
            name: '经理/主管',
            icon: '👨‍💼',
            iconUrl: '/assets/icons/3d_flat_job_admin.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_job_logistics',
        name: '司机/仓储',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_job_logistics.png',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_logistics_driver',
            name: '专职司机',
            icon: '🚘',
            iconUrl: '/assets/icons/3d_flat_job_logistics.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-12K', '12K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '高提成'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_logistics_delivery',
            name: '快递/外卖骑手',
            icon: '🛵',
            iconUrl: '/assets/icons/3d_flat_job_logistics.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-12K', '12K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '高提成'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_logistics_warehouse',
            name: '仓储/理货',
            icon: '📦',
            iconUrl: '/assets/icons/3d_flat_job_logistics.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-12K', '12K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '高提成'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_job_other',
        name: '教育/医疗',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_job_other.png',
        sortOrder: 6,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_other_edu',
            name: '教师/培训',
            icon: '👩‍🏫',
            iconUrl: '/assets/icons/3d_flat_job_other.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          },
          {
            id: 'job_other_med',
            name: '医生/护士',
            icon: '⚕️',
            iconUrl: '/assets/icons/3d_flat_job_other.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          },
          {
            id: 'job_other_all',
            name: '其他职位',
            icon: '📌',
            iconUrl: '/assets/icons/3d_flat_job_other.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_pt_temp',
        name: '日结/临工',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_pt_temp.png',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'pt_temp_daily',
            name: '日结临时工',
            icon: '👷',
            iconUrl: '/assets/icons/3d_flat_pt_temp.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['白天全天', '夜班'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 150元/天' },
            ]),
          },
          {
            id: 'pt_temp_hourly',
            name: '小时工',
            icon: '⏱️',
            iconUrl: '/assets/icons/3d_flat_pt_temp.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '完工结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['自由时间', '指定时段'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 20元/小时' },
            ]),
          }
        ]
      },
      {
        id: 'cat_pt_promo',
        name: '地推/促销',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_pt_promo.png',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'pt_promo_flyer',
            name: '派发传单',
            icon: '📄',
            iconUrl: '/assets/icons/3d_flat_pt_promo.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '完工结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['周末', '特定时间段'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 20元/小时' },
            ]),
          },
          {
            id: 'pt_promo_sales',
            name: '促销员/导购',
            icon: '📢',
            iconUrl: '/assets/icons/3d_flat_pt_promo.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '周结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['周末', '法定节假日'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 150元/天+提成' },
            ]),
          },
          {
            id: 'pt_promo_audience',
            name: '充场/会展协助',
            icon: '🎭',
            iconUrl: '/assets/icons/3d_flat_pt_promo.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '完工结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['特定时间段'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 50元/半天' },
            ]),
          }
        ]
      },
      {
        id: 'cat_pt_hotel',
        name: '餐饮/客房',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_pt_hotel.png',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'pt_hotel_waiter',
            name: '餐厅服务员',
            icon: '🍽️',
            iconUrl: '/assets/icons/3d_flat_pt_hotel.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '周结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['周末', '晚上', '用餐高峰期'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 18元/小时' },
            ]),
          },
          {
            id: 'pt_hotel_kitchen',
            name: '后厨帮工/洗碗',
            icon: '🧼',
            iconUrl: '/assets/icons/3d_flat_pt_hotel.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '周结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['全天', '半天'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 120元/天' },
            ]),
          }
        ]
      },
      {
        id: 'cat_pt_tutor',
        name: '家教/培训',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_pt_tutor.png',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'pt_tutor_school',
            name: '中小学家教',
            icon: '📚',
            iconUrl: '/assets/icons/3d_flat_pt_tutor.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['完工结', '月结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['周末', '晚上'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 50元/小时' },
            ]),
          },
          {
            id: 'pt_tutor_art',
            name: '艺术/特长培训',
            icon: '🎨',
            iconUrl: '/assets/icons/3d_flat_pt_tutor.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['完工结', '月结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['周末', '自由时间'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 80元/节课' },
            ]),
          }
        ]
      },
      {
        id: 'cat_pt_errand',
        name: '跑腿/代办',
        icon: 'folder',
        iconUrl: '/assets/icons/3d_flat_pt_errand.png',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'pt_errand_delivery',
            name: '同城代送/代买',
            icon: '🛵',
            iconUrl: '/assets/icons/3d_flat_pt_errand.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['完工结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['随时', '自由时间'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 15元/单' },
            ]),
          },
          {
            id: 'pt_errand_service',
            name: '代排队/代办',
            icon: '🧍',
            iconUrl: '/assets/icons/3d_flat_pt_errand.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['完工结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['指定时间'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 30元/小时' },
            ]),
          }
        ]
      },
    ],

  },
  // 7. 租车服务
  {
    id: 'cat_car_rental',
    name: '拼车/租车',
    icon: 'local-shipping',
    iconUrl: '/assets/icons/3d_flat_car_rental.png',
    sortOrder: 7,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'car_carpool_person_find_car',
        name: '人找车',
        icon: '🙋',
            iconUrl: '/assets/icons/3d_flat_car_carpool_person.png',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'departure', label: '出发地', fieldType: 'TEXT', required: true, placeholder: '例如: 连山县城' },
          { key: 'destination', label: '目的地', fieldType: 'TEXT', required: true, placeholder: '例如: 清远市区' },
          { key: 'time', label: '出发时间', fieldType: 'TEXT', required: true, placeholder: '例如: 明早8点' },
          { key: 'people', label: '乘车人数', fieldType: 'TEXT', required: true, placeholder: '例如: 2人' },
        ]),
      },
      {
        id: 'car_carpool_car_find_person',
        name: '车找人',
        icon: '🚘',
            iconUrl: '/assets/icons/3d_flat_car_suv.png',
        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'departure', label: '出发地', fieldType: 'TEXT', required: true, placeholder: '例如: 连山' },
          { key: 'destination', label: '目的地', fieldType: 'TEXT', required: true, placeholder: '例如: 广州' },
          { key: 'time', label: '出发时间', fieldType: 'TEXT', required: true, placeholder: '例如: 今晚6点' },
          { key: 'seats', label: '空余座位', fieldType: 'TEXT', required: true, placeholder: '例如: 3座' },
        ]),
      },
      {
        id: 'car_carpool_goods',
        name: '捎带货',
        icon: '📦',
            iconUrl: '/assets/icons/3d_flat_pt_errand.png',
        sortOrder: 3,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'route', label: '起止路线', fieldType: 'TEXT', required: true, placeholder: '例如: 连山-连州' },
          { key: 'goods', label: '货物描述', fieldType: 'TEXT', required: true, placeholder: '例如: 两个小纸箱' },
        ]),
      },
      {
        id: 'car_rental_suv',
            iconUrl: '/assets/icons/3d_flat_car_suv.png',
        name: '轿车 / SUV 租赁',
        icon: '🚗',
        sortOrder: 4,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'carType', label: '车型', fieldType: 'TEXT', required: true, placeholder: '例如: 丰田卡罗拉' },
          { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['日租', '月租'] },
        ]),
      },
      {
        id: 'car_rental_wedding',
        name: '婚车租赁',
        icon: '🎀',
            iconUrl: '/assets/icons/3d_flat_car_luxury.png',
        sortOrder: 5,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'carType', label: '婚车车型', fieldType: 'TEXT', required: true, placeholder: '例如: 奔驰S级 / 宝马7系' },
          { key: 'rentType', label: '服务包含', fieldType: 'SELECT', required: true, options: ['带司机', '自驾'] },
        ]),
      },
      {
        id: 'car_rental_bus',
        name: '大巴商务车租赁',
        icon: '🚌',
            iconUrl: '/assets/icons/3d_flat_car_bus.png',
        sortOrder: 6,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'carType', label: '车型', fieldType: 'TEXT', required: true, placeholder: '例如: 别克GL8 / 考斯特' },
          { key: 'seats', label: '座位数', fieldType: 'TEXT', required: true, placeholder: '例如: 7座 / 20座' },
        ]),
      },
      {
        id: 'car_rental_truck',
        name: '货车/工程车租赁',
        icon: '🚚',
            iconUrl: '/assets/icons/3d_flat_car_truck.png',
        sortOrder: 7,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'carType', label: '车辆类型', fieldType: 'TEXT', required: true, placeholder: '例如: 4.2米箱货 / 挖掘机' },
        ]),
      }
    ],
  },
  // 9. 教育培训
  {
    id: 'cat_education',
    name: '教育培训',
    icon: 'school',
    iconUrl: '/assets/icons/3d_flat_education.png',
    sortOrder: 9,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'edu_k12',
        name: '中小学辅导',
        icon: '📚',

        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'subject', label: '科目', fieldType: 'TEXT', required: true, placeholder: '例如: 数学/英语' },
        ]),
      },
      {
        id: 'edu_art',
        name: '艺术',
        icon: '🎨',

        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'subject', label: '类型', fieldType: 'TEXT', required: true, placeholder: '例如: 美术/音乐' },
        ]),
      },
      {
        id: 'edu_driving',
        name: '驾校招考',
        icon: '🚗',

        sortOrder: 3,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'licenseType', label: '驾照类型', fieldType: 'SELECT', required: true, options: ['C1', 'C2', 'B2', 'A1'] },
        ]),
      },
      {
        id: 'edu_vocational',
        name: '职业技能',
        icon: '🔧',

        sortOrder: 4,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'skill', label: '技能方向', fieldType: 'TEXT', required: true, placeholder: '例如: 厨师/电工/美容' },
        ]),
      },
      {
        id: 'edu_english',
        name: '英语',
        icon: '🔤',

        sortOrder: 5,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'level', label: '适合水平', fieldType: 'TEXT', required: true, placeholder: '例如: 四六级/雅思' },
        ]),
      },
      {
        id: 'edu_ai_science',
        name: 'AI编程/科学',
        icon: '💻',

        sortOrder: 6,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'course', label: '课程内容', fieldType: 'TEXT', required: true, placeholder: '例如: 少儿编程/机器人' },
        ]),
      }
    ],
  },
  // 10. 餐饮娱乐
  {
    id: 'cat_dining',
    name: '餐饮娱乐',
    icon: 'restaurant',
    iconUrl: '/assets/icons/3d_flat_dining.png',
    sortOrder: 10,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'cat_dining_food',
        name: '餐饮类',
        icon: '🍽️',
        iconUrl: '/assets/icons/3d_flat_dining.png',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'dining_fast_food',
            name: '快餐便当',
            icon: '🍔',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 15元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 猪脚饭/盖浇饭' },
            ]),
          },
          {
            id: 'dining_drinks',
            name: '奶茶饮品',
            icon: '🧋',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 10元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 幽兰拿铁/手打柠檬茶' },
            ]),
          },
          {
            id: 'dining_bbq',
            name: '烧烤海鲜',
            icon: '🍢',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 50元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 烤生蚝/小龙虾' },
            ]),
          },
          {
            id: 'dining_hotpot',
            name: '火锅小吃',
            icon: '🍲',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 80元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 重庆老火锅/潮汕牛肉' },
            ]),
          },
          {
            id: 'dining_restaurant',
            name: '地方菜系',
            icon: '🍽️',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 5,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 60元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 湘菜/农家乐' },
            ]),
          },
          {
            id: 'dining_bakery',
            name: '甜点烘焙',
            icon: '🍰',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 6,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 30元' },
              { key: 'specialty', label: '招牌特色', fieldType: 'TEXT', required: true, placeholder: '例如: 生日蛋糕/脏脏包' },
            ]),
          }
        ]
      },
      {
        id: 'cat_dining_entertainment',
        name: '娱乐休闲类',
        icon: '🎮',
        iconUrl: '/assets/icons/3d_flat_dining.png',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'dining_ktv_bar',
            name: 'KTV / 酒吧',
            icon: '🎤',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 80元' },
              { key: 'facilities', label: '配套设施', fieldType: 'TEXT', required: true, placeholder: '例如: 免费停车/豪华包间' },
            ]),
          },
          {
            id: 'dining_massage',
            name: '洗浴足疗',
            icon: '💆',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'duration', label: '服务时长', fieldType: 'TEXT', required: true, placeholder: '例如: 60分钟' },
              { key: 'therapist', label: '技师介绍', fieldType: 'TEXT', required: true, placeholder: '例如: 金牌技师/多年经验' },
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 120元' },
            ]),
          },
          {
            id: 'dining_billiards_cafe',
            name: '台球网咖',
            icon: '🎱',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 30元' },
              { key: 'facilities', label: '配套设施', fieldType: 'TEXT', required: true, placeholder: '例如: 星牌台球/RTX4060电脑' },
            ]),
          },
          {
            id: 'dining_mahjong_tea',
            name: '棋牌茶室',
            icon: '🀄',
            iconUrl: '/assets/icons/3d_flat_dining.png',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'avgPrice', label: '人均消费', fieldType: 'TEXT', required: true, placeholder: '例如: 50元' },
              { key: 'facilities', label: '配套设施', fieldType: 'TEXT', required: true, placeholder: '例如: 机麻包厢/免费茶水' },
            ]),
          }
        ]
      }
    ],
  }
];

async function seedCategories(nodes: any[], parentId: string | null = null) {
  for (const node of nodes) {
    await prisma.category.upsert({
      where: { id: node.id },
      update: {
        name: node.name,
        icon: node.icon || null,
        iconUrl: node.iconUrl || null,
        parentId,
        sortOrder: node.sortOrder || 0,
        isLeaf: node.isLeaf || false,
        isActive: node.isActive !== undefined ? node.isActive : true,
        isHot: node.isHot !== undefined ? node.isHot : false,
        attributeSchema: node.attributeSchema || '[]',
      },
      create: {
        id: node.id,
        name: node.name,
        icon: node.icon || null,
        iconUrl: node.iconUrl || null,
        parentId,
        sortOrder: node.sortOrder || 0,
        isLeaf: node.isLeaf || false,
        isActive: node.isActive !== undefined ? node.isActive : true,
        isHot: node.isHot !== undefined ? node.isHot : false,
        attributeSchema: node.attributeSchema || '[]',
      },
    });

    if (node.children && node.children.length > 0) {
      await seedCategories(node.children, node.id);
    }
  }
}

async function main() {
  console.log('清空旧分类数据...');
  await prisma.post.updateMany({
    where: { categoryId: { not: null } },
    data: { categoryId: null }
  });
  await prisma.category.deleteMany({});
  console.log('旧分类数据清空完毕！');

  console.log('开始导入分类树与动态表单 Schema...');
  await seedCategories(categoryTreeSeed);
  console.log('分类树导入完成！');

  if (process.env.SEED_MOCK === 'true') {
    console.log('开始导入商家与商品数据...');
    for (const m of merchantsData) {
      const merchant = await prisma.merchant.upsert({
        where: { externalId: m.id },
        update: {},
        create: {
          externalId: m.id,
          name: m.name,
          rating: m.rating,
          distance: m.distance,
          sales: m.sales,
          avgPrice: m.avgPrice,
          tags: JSON.stringify(m.tags),
          deliveryFee: m.deliveryFee,
          deliveryTime: m.deliveryTime,
          logo: m.logo,
          banner: m.banner,
          isFood: m.isFood,
          category: m.category,
          latitude: m.latitude,
          longitude: m.longitude,
          description: m.description,
          address: m.address,
          phone: m.phone,
        },
      });

      for (const item of m.items) {
        await prisma.product.upsert({
          where: { externalId: item.id },
          update: {},
          create: {
            externalId: item.id,
            merchantId: merchant.id,
            name: item.name,
            price: item.price,
            originalPrice: item.originalPrice,
            desc: item.desc,
            sales: item.sales,
            image: item.image,
            category: item.category,
            rating: item.rating,
          },
        });
      }
    }
  } else {
    console.log('跳过模拟商家与商品数据导入 (如需导入模拟数据请设置 SEED_MOCK=true)');
  }

  const categoryCount = await prisma.category.count();
  const merchantCount = await prisma.merchant.count();
  const productCount = await prisma.product.count();
  console.log(`导入完成: ${categoryCount} 个分类, ${merchantCount} 个商家, ${productCount} 个商品`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
