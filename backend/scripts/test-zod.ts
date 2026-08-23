import { z } from 'zod';

const ALLOWED_CATEGORIES = [
  'second_hand',
  'job',
  'part_time',
  'house',
  'secondhand_house',
  'shop_rent',
  'housekeeping',
  'maintenance',
  'moving',
  'veggies',
  'car_rental',
] as const;

const schema = z.object({
  category: z.enum(ALLOWED_CATEGORIES),
  title: z.string().max(60).nullable().optional(),
  description: z.string().min(1).max(2000),
  price: z.number().nonnegative().optional().nullable(),
  images: z.array(z.string().min(8).max(500)).max(9).default([]),
  latitude: z.number().nullable().optional(),
  longitude: z.number().nullable().optional(),
  locationName: z.string().max(80).nullable().optional(),
  publisherType: z.enum(['INDIVIDUAL', 'MERCHANT']).default('INDIVIDUAL'),
  merchantId: z.string().nullable().optional(),
  listingType: z.enum(['GOODS', 'SERVICE']).default('GOODS'),
  attributes: z.record(z.string(), z.string()).optional().default({}),
});

const payload = {
  category: "second_hand",
  title: "Insta360 Flow2 Pro云台",
  description: "我这里有一款非常不错的Insta360 Flow2 Pro云台...",
  price: null,
  images: ["http://localhost:4000/uploads/123.jpg"],
  publisherType: "INDIVIDUAL",
  merchantId: null,
  listingType: "GOODS",
  attributes: {
    brand: "Insta360",
    condition: "全新"
  },
  locationName: "连山壮族瑶族自治县"
};

try {
  schema.parse(payload);
  console.log("Success!");
} catch (e) {
  console.log(e);
}
