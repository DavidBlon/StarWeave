import { useEffect, useRef, useMemo } from 'react'

// 模拟地球视角的星空
export default function StarField({ paused = false }) {
  const canvasRef = useRef(null)
  const frameRef = useRef(0)

  // 生成星星数据
  const stars = useMemo(() => {
    const stars = []
    const STAR_COUNT = 800

    for (let i = 0; i < STAR_COUNT; i++) {
      // 随机位置（球坐标，模拟天球）
      const theta = Math.random() * Math.PI * 2 // 方位角
      const phi = Math.acos(2 * Math.random() - 1) // 仰角

      // 转换为笛卡尔坐标（天球半径 500）
      const radius = 500
      const x = radius * Math.sin(phi) * Math.cos(theta)
      const y = radius * Math.cos(phi) // Y 轴朝上（天顶）
      const z = radius * Math.sin(phi) * Math.sin(theta)

      // 随机大小和亮度
      const size = Math.random() * 2 + 0.5
      const brightness = Math.random() * 0.7 + 0.3

      // 随机颜色（基于恒星光谱类型）
      const colorTypes = [
        { color: '#9bb0ff', weight: 0.1 }, // 蓝色（O/B型）
        { color: '#aabfff', weight: 0.15 }, // 蓝白色（A型）
        { color: '#cad7ff', weight: 0.2 }, // 白色（F型）
        { color: '#f8f7ff', weight: 0.25 }, // 黄白色（G型）
        { color: '#fff4ea', weight: 0.15 }, // 黄色（K型）
        { color: '#ffd2a1', weight: 0.1 }, // 橙色（K型）
        { color: '#ffcc6f', weight: 0.05 }, // 红色（M型）
      ]

      // 根据权重选择颜色
      let rand = Math.random()
      let color = '#ffffff'
      let cumWeight = 0
      for (const type of colorTypes) {
        cumWeight += type.weight
        if (rand <= cumWeight) {
          color = type.color
          break
        }
      }

      // 闪烁参数
      const twinkleSpeed = Math.random() * 0.02 + 0.005
      const twinklePhase = Math.random() * Math.PI * 2

      stars.push({
        x, y, z,
        size,
        brightness,
        color,
        twinkleSpeed,
        twinklePhase,
        originalBrightness: brightness
      })
    }

    // 添加一些特别亮的恒星（模拟一等星）
    const brightStars = [
      { name: 'Sirius', ra: 101.2872, dec: -16.7161, color: '#aabfff' },
      { name: 'Canopus', ra: 95.9880, dec: -52.6957, color: '#f8f7ff' },
      { name: 'Arcturus', ra: 213.9154, dec: 19.1825, color: '#ffd2a1' },
      { name: 'Vega', ra: 279.2347, dec: 38.7837, color: '#cad7ff' },
      { name: 'Capella', ra: 79.1723, dec: 45.9980, color: '#fff4ea' },
      { name: 'Rigel', ra: 78.6345, dec: -8.2016, color: '#aabfff' },
      { name: 'Procyon', ra: 114.8255, dec: 5.2250, color: '#f8f7ff' },
      { name: 'Betelgeuse', ra: 88.7929, dec: 7.4071, color: '#ffcc6f' },
      { name: 'Altair', ra: 297.6958, dec: 8.8683, color: '#cad7ff' },
      { name: 'Deneb', ra: 310.3580, dec: 45.2803, color: '#cad7ff' },
      { name: 'Spica', ra: 201.2983, dec: -11.1614, color: '#aabfff' },
      { name: 'Antares', ra: 247.3519, dec: -26.4320, color: '#ffcc6f' },
    ]

    brightStars.forEach(star => {
      const raRad = star.ra * Math.PI / 180
      const decRad = star.dec * Math.PI / 180
      const radius = 500

      const x = radius * Math.cos(decRad) * Math.sin(raRad)
      const y = radius * Math.sin(decRad)
      const z = radius * Math.cos(decRad) * Math.cos(raRad)

      stars.push({
        x, y, z,
        size: 3 + Math.random() * 2,
        brightness: 0.9 + Math.random() * 0.1,
        color: star.color,
        twinkleSpeed: Math.random() * 0.01 + 0.003,
        twinklePhase: Math.random() * Math.PI * 2,
        originalBrightness: 0.9 + Math.random() * 0.1,
        isBright: true
      })
    })

    return stars
  }, [])

  // 生成星座连线
  const constellationLines = useMemo(() => {
    const lines = []

    // 猎户座
    const orionStars = [
      { ra: 88.7929, dec: 7.4071 }, // 参宿四
      { ra: 81.2828, dec: 6.3497 }, // 参宿五
      { ra: 83.0017, dec: -0.2991 }, // 参宿一
      { ra: 84.0534, dec: -1.2019 }, // 参宿二
      { ra: 85.1897, dec: -1.9426 }, // 参宿三
      { ra: 86.9391, dec: -9.6696 }, // 参宿六
      { ra: 78.6345, dec: -8.2016 }, // 参宿七
    ]

    // 猎户座连线
    const orionConnections = [
      [0, 1], [1, 2], [2, 3], [3, 4], [4, 5], [5, 6], [6, 0]
    ]

    orionConnections.forEach(([startIdx, endIdx]) => {
      const start = orionStars[startIdx]
      const end = orionStars[endIdx]

      const raRad1 = start.ra * Math.PI / 180
      const decRad1 = start.dec * Math.PI / 180
      const raRad2 = end.ra * Math.PI / 180
      const decRad2 = end.dec * Math.PI / 180
      const radius = 500

      lines.push({
        start: {
          x: radius * Math.cos(decRad1) * Math.sin(raRad1),
          y: radius * Math.sin(decRad1),
          z: radius * Math.cos(decRad1) * Math.cos(raRad1)
        },
        end: {
          x: radius * Math.cos(decRad2) * Math.sin(raRad2),
          y: radius * Math.sin(decRad2),
          z: radius * Math.cos(decRad2) * Math.cos(raRad2)
        }
      })
    })

    // 大熊座（北斗七星）
    const ursaMajorStars = [
      { ra: 165.9322, dec: 61.7510 }, // 天枢
      { ra: 165.4603, dec: 56.3824 }, // 天璇
      { ra: 178.4577, dec: 53.6948 }, // 天玑
      { ra: 183.8565, dec: 57.0326 }, // 天权
      { ra: 193.5073, dec: 55.9598 }, // 玉衡
      { ra: 200.9814, dec: 54.9254 }, // 开阳
      { ra: 206.8852, dec: 49.3133 }, // 摇光
    ]

    const ursaMajorConnections = [
      [0, 1], [1, 2], [2, 3], [3, 4], [4, 5], [5, 6]
    ]

    ursaMajorConnections.forEach(([startIdx, endIdx]) => {
      const start = ursaMajorStars[startIdx]
      const end = ursaMajorStars[endIdx]

      const raRad1 = start.ra * Math.PI / 180
      const decRad1 = start.dec * Math.PI / 180
      const raRad2 = end.ra * Math.PI / 180
      const decRad2 = end.dec * Math.PI / 180
      const radius = 500

      lines.push({
        start: {
          x: radius * Math.cos(decRad1) * Math.sin(raRad1),
          y: radius * Math.sin(decRad1),
          z: radius * Math.cos(decRad1) * Math.cos(raRad1)
        },
        end: {
          x: radius * Math.cos(decRad2) * Math.sin(raRad2),
          y: radius * Math.sin(decRad2),
          z: radius * Math.cos(decRad2) * Math.cos(raRad2)
        }
      })
    })

    return lines
  }, [])

  // 渲染循环
  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const ctx = canvas.getContext('2d')
    let animationId

    // 设置 canvas 大小
    const resize = () => {
      canvas.width = window.innerWidth
      canvas.height = window.innerHeight
    }
    resize()
    window.addEventListener('resize', resize)

    // 相机参数
    const camera = {
      position: { x: 0, y: 0, z: 0 },
      lookAt: { x: 0, y: 1, z: 0 },
      fov: 90,
      aspect: canvas.width / canvas.height
    }

    // 投影函数
    function project(point) {
      // 简单的透视投影
      const fovRad = camera.fov * Math.PI / 180
      const fovFactor = 1 / Math.tan(fovRad / 2)

      // 相机朝向天顶，所以需要旋转坐标
      // 假设相机朝向 Y 轴正方向
      const rotatedX = point.x
      const rotatedY = point.y
      const rotatedZ = point.z

      // 透视投影
      if (rotatedZ <= 0) return null // 在相机后方

      const scale = fovFactor / rotatedZ
      const screenX = (rotatedX * scale * 0.5 + 0.5) * canvas.width
      const screenY = (1 - (rotatedY * scale * 0.5 + 0.5)) * canvas.height

      return { x: screenX, y: screenY, scale }
    }

    // 渲染函数
    function render(time) {
      if (paused) {
        animationId = requestAnimationFrame(render)
        return
      }

      ctx.clearRect(0, 0, canvas.width, canvas.height)

      // 绘制背景渐变（模拟夜空）
      const gradient = ctx.createRadialGradient(
        canvas.width / 2, canvas.height / 2, 0,
        canvas.width / 2, canvas.height / 2, canvas.width * 0.7
      )
      gradient.addColorStop(0, 'rgba(10, 10, 26, 1)')
      gradient.addColorStop(1, 'rgba(5, 5, 15, 1)')
      ctx.fillStyle = gradient
      ctx.fillRect(0, 0, canvas.width, canvas.height)

      // 绘制星座连线
      constellationLines.forEach(line => {
        const start = project(line.start)
        const end = project(line.end)

        if (start && end) {
          ctx.beginPath()
          ctx.moveTo(start.x, start.y)
          ctx.lineTo(end.x, end.y)
          ctx.strokeStyle = 'rgba(100, 130, 180, 0.15)'
          ctx.lineWidth = 0.5
          ctx.stroke()
        }
      })

      // 绘制星星
      stars.forEach(star => {
        // 更新闪烁
        star.twinklePhase += star.twinkleSpeed
        const twinkle = Math.sin(star.twinklePhase) * 0.2 + 0.8
        star.brightness = star.originalBrightness * twinkle

        const projected = project(star)
        if (!projected) return

        // 绘制星星
        const size = star.size * projected.scale * 200
        const alpha = star.brightness

        // 解析颜色
        const color = star.color
        const r = parseInt(color.slice(1, 3), 16)
        const g = parseInt(color.slice(3, 5), 16)
        const b = parseInt(color.slice(5, 7), 16)

        // 绘制发光效果
        if (star.isBright) {
          const glowSize = size * 3
          const glowGradient = ctx.createRadialGradient(
            projected.x, projected.y, 0,
            projected.x, projected.y, glowSize
          )
          glowGradient.addColorStop(0, `rgba(${r}, ${g}, ${b}, ${alpha * 0.3})`)
          glowGradient.addColorStop(1, `rgba(${r}, ${g}, ${b}, 0)`)
          ctx.fillStyle = glowGradient
          ctx.beginPath()
          ctx.arc(projected.x, projected.y, glowSize, 0, Math.PI * 2)
          ctx.fill()
        }

        // 绘制星星核心
        ctx.fillStyle = `rgba(${r}, ${g}, ${b}, ${alpha})`
        ctx.beginPath()
        ctx.arc(projected.x, projected.y, size, 0, Math.PI * 2)
        ctx.fill()
      })

      // 绘制流星（偶尔出现）
      if (Math.random() < 0.001) { // 0.1% 概率出现流星
        drawMeteor(ctx, canvas)
      }

      frameRef.current++
      animationId = requestAnimationFrame(render)
    }

    // 绘制流星
    function drawMeteor(ctx, canvas) {
      const startX = Math.random() * canvas.width
      const startY = Math.random() * canvas.height * 0.5
      const length = 100 + Math.random() * 200
      const angle = Math.PI / 4 + Math.random() * Math.PI / 2

      const endX = startX + Math.cos(angle) * length
      const endY = startY + Math.sin(angle) * length

      const gradient = ctx.createLinearGradient(startX, startY, endX, endY)
      gradient.addColorStop(0, 'rgba(255, 255, 255, 0.8)')
      gradient.addColorStop(0.5, 'rgba(139, 233, 253, 0.6)')
      gradient.addColorStop(1, 'rgba(139, 233, 253, 0)')

      ctx.beginPath()
      ctx.moveTo(startX, startY)
      ctx.lineTo(endX, endY)
      ctx.strokeStyle = gradient
      ctx.lineWidth = 2
      ctx.stroke()
    }

    animationId = requestAnimationFrame(render)

    return () => {
      cancelAnimationFrame(animationId)
      window.removeEventListener('resize', resize)
    }
  }, [paused, stars, constellationLines])

  return <canvas ref={canvasRef} id="star-canvas" />
}
