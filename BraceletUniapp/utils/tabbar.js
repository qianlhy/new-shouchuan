/** 同步微信自定义 tabBar 选中态 */
export function setTabBarSelected(index) {
  // #ifdef MP-WEIXIN
  try {
    const pages = getCurrentPages()
    const page = pages[pages.length - 1]
    if (!page || typeof page.getTabBar !== 'function') return
    const tabBar = page.getTabBar()
    if (!tabBar) return
    if (typeof tabBar.setData === 'function') {
      tabBar.setData({ selected: index })
    } else if (tabBar.$vm) {
      tabBar.$vm.selected = index
    } else {
      tabBar.selected = index
    }
  } catch (e) {
    /* ignore */
  }
  // #endif
}
