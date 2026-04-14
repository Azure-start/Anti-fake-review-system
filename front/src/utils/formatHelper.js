export function formatTime(time) {
    if (!time) return '-'
    const date = new Date(time)
    return date.toLocaleString('zh-CN')
}