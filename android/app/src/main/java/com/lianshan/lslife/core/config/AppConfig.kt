package com.lianshan.lslife.core.config

/**
 * Global application configuration flags.
 * Toggle feature flags to switch between full e-commerce mode and light info/IM contact mode.
 */
object AppConfig {
    /**
     * Controls whether shopping cart, checkout, and online order creation features are enabled.
     * When false, cart entries, checkout buttons, and order creation are hidden or redirected to IM/Phone contact.
     */
    const val ENABLE_COMMERCE_CART = false
}
