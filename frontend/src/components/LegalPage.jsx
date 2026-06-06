import { useState, useCallback } from 'react';

const AGREEMENT_CONTENT = `
# 流星树洞 · 用户协议

**生效日期：2025 年 6 月 1 日**

欢迎使用"流星树洞·织星海"（以下简称"本平台"）。在使用本平台提供的服务前，请你仔细阅读并充分理解本协议的全部内容。你通过网络页面点击"同意"或实际使用本平台服务，即表示你已阅读并同意接受本协议的约束。

---

## 一、服务说明

1.1 本平台是一个匿名情感分享社区，你可以在这里以匿名方式发布心事（"流星"），也可以捞起他人发布的流星进行阅读和回复。

1.2 本平台提供的服务包括但不限于：发布流星、捞起流星、生成星图、查看个人统计等。

1.3 本平台保留随时变更、暂停或终止部分或全部服务的权利，届时将通过平台内通知方式告知用户。

---

## 二、用户行为规范

2.1 你承诺在使用本平台时遵守中华人民共和国相关法律法规，不得利用本平台制作、复制、发布、传播含有以下内容的信息（包括流星和回复）：

- 反对宪法所确定的基本原则的；
- 危害国家安全、泄露国家秘密、颠覆国家政权、破坏国家统一的；
- 损害国家荣誉和利益的；
- 煽动民族仇恨、民族歧视、破坏民族团结的；
- 破坏国家宗教政策、宣扬邪教和封建迷信的；
- 散布谣言、扰乱社会秩序、破坏社会稳定的；
- 散布淫秽、色情、赌博、暴力、凶杀、恐怖或教唆犯罪的；
- 侮辱或诽谤他人、侵害他人合法权益的；
- 含有法律、行政法规禁止的其他内容的。

2.2 你不得利用本平台从事以下行为：

- 对平台进行反向工程、反向汇编、反向编译或以其他方式尝试获取源代码；
- 利用技术手段或其他方式干扰平台正常运行；
- 未经授权批量获取平台数据；
- 冒充他人或使用虚假信息注册账号。

2.3 本平台有权对用户发布的内容进行审核，对违反本协议或法律法规的内容进行删除处理，并视情节轻重对账号进行警告、限制功能或封禁处理。

---

## 三、内容与知识产权

3.1 你在本平台发布的内容（文字、图片等）的知识产权归你所有。你同意授予本平台在全球范围内、免费的、非独家的使用权，用于本平台的运营、推广和改善。

3.2 本平台中的软件、技术、界面设计、logo 等内容的知识产权归本平台所有，未经书面许可，任何人不得以任何形式使用。

3.3 你理解并同意，由于本平台的匿名特性，你发布的内容将对其他用户可见（在通过审核后），且你无法主动撤回已被他人阅读的内容。

---

## 四、虚拟商品与付费服务

4.1 本平台可能提供付费虚拟商品或增值服务，包括但不限于高清星图下载、AI 回信等。

4.2 虚拟商品一经售出，除法律另有规定外，不支持退款。购买前请确认商品内容。

4.3 本平台保留调整虚拟商品价格的权利，调整前将通过合理方式通知用户。

---

## 五、免责声明

5.1 本平台不对用户发布内容的真实性、准确性、合法性负责。

5.2 因网络状况、通信线路等不可抗力导致的服务中断或数据丢失，本平台不承担责任。

5.3 你因使用本平台服务与第三方发生纠纷的，本平台不承担任何责任。

---

## 六、协议变更

6.1 本平台有权根据需要不时地修改本协议条款，修改后的协议将在平台内公布。

6.2 如果你不同意修改后的协议，你有权停止使用本平台。如果你继续使用，则视为你接受修改后的协议。

---

## 七、适用法律与争议解决

7.1 本协议的签订、履行和解释均适用中华人民共和国法律。

7.2 因本协议引起的或与本协议有关的任何争议，双方应友好协商解决；协商不成的，任何一方均有权向本平台所在地人民法院提起诉讼。

---

如有疑问，请通过平台内反馈功能联系我们。
`.trim();

const POLICY_CONTENT = `
# 流星树洞 · 隐私政策

**生效日期：2025 年 6 月 1 日**

"流星树洞·织星海"（以下简称"我们"或"本平台"）非常重视你的隐私保护。本隐私政策旨在向你说明我们如何收集、使用、存储和保护你的个人信息。

---

## 一、我们收集的信息

### 1.1 你主动提供的信息

- **账号信息**：用户名、昵称、密码（加密存储）
- **个人资料**：头像、个人签名
- **发布内容**：你发布的流星文字、回复（许愿）内容

### 1.2 自动收集的信息

- **设备信息**：设备型号、操作系统版本（用于兼容性适配）
- **日志信息**：访问时间、操作记录（用于安全审计和问题排查）

### 1.3 我们不会收集的信息

- 你的真实姓名、身份证号、电话号码、地址等敏感个人信息
- 你的通讯录、相册（除非你主动上传头像）、位置信息

---

## 二、我们如何使用信息

我们收集的信息将用于以下目的：

- **提供服务**：维护你的账号、展示你发布的内容、生成星图
- **内容审核**：对发布内容（包括流星和回复）进行合规审核，维护社区环境
- **安全防护**：防范欺诈、攻击等安全风险
- **服务改善**：分析使用情况，优化产品体验
- **合规义务**：根据法律法规要求进行必要的信息留存

---

## 三、信息的存储与保护

### 3.1 存储地点

你的个人信息存储在中华人民共和国境内的服务器上。

### 3.2 存储期限

我们仅在实现本政策所述目的所需的期限内保留你的个人信息。当你注销账号后，我们将在合理期限内删除你的个人信息，但法律法规另有规定的除外。

### 3.3 安全措施

我们采取以下措施保护你的信息安全：

- 密码使用 SHA-256 等加密算法存储，我们无法获知你的明文密码
- 传输层使用 HTTPS 加密
- 访问控制和权限管理
- 定期安全审计

---

## 四、信息的共享与披露

4.1 未经你的同意，我们不会向第三方共享你的个人信息，但以下情况除外：

- 根据法律法规的要求、行政或司法机关的要求；
- 为保护本平台、其用户或公众的合法权益；
- 在合并、收购或破产清算等情形下涉及的个人信息转移。

4.2 我们可能会与第三方服务提供商共享必要的信息，用于技术支持、数据存储等服务。我们会要求这些服务提供商遵守本隐私政策。

---

## 五、你的权利

你有权：

- **访问**：查看你的个人信息
- **更正**：修改你的昵称、头像、签名等个人资料
- **删除**：删除你发布的流星和回复内容（回复删除后不可恢复）
- **注销**：注销你的账号，届时我们将删除你的所有个人信息和发布内容
- **撤回同意**：撤回你之前给予的同意

如需行使上述权利，请通过平台内反馈功能联系我们。

---

## 六、内容管理与审核

6.1 你发布的流星内容和回复（许愿）在通过审核后方可公开展示。审核中的内容仅自己可见。

6.2 你可以随时删除自己发布的回复（许愿）。删除后该内容将从平台移除，不可恢复。

6.3 管理员有权对平台内所有内容（包括流星和回复）进行审核、删除或隐藏处理，无需事先通知。

6.4 管理员有权删除违规用户账号及其发布的所有内容，删除后数据不可恢复。

6.5 被删除的内容（包括用户自行删除和管理员删除）均无法恢复，请谨慎操作。

---

## 七、未成年人保护

6.1 如果你是未满 18 周岁的未成年人，请在法定监护人的陪同下阅读本隐私政策，并在监护人同意后使用本平台。

6.2 我们不会主动收集未成年人的个人信息。如果我们发现在未获得监护人同意的情况下收集了未成年人的个人信息，我们将尽快删除相关信息。

6.3 我们建议未成年人在使用互联网时加强自我保护意识，在发布个人信息前征得监护人同意。

---

## 八、隐私政策的变更

7.1 我们可能会不时更新本隐私政策。更新后的政策将在平台内公布，并注明生效日期。

7.2 对于重大变更，我们将通过平台内通知等方式告知你。

---

## 九、联系我们

如果你对本隐私政策有任何疑问、意见或建议，请通过以下方式联系我们：

- 通过平台内"反馈"功能提交

我们将在 15 个工作日内回复你的请求。
`.trim();

export default function LegalPage({ defaultTab = 'agreement', onClose }) {
  const [tab, setTab] = useState(defaultTab);
  const [leaving, setLeaving] = useState(false);

  const handleBack = useCallback(() => {
    setLeaving(true);
    setTimeout(() => onClose?.(), 250);
  }, [onClose]);

  const content = tab === 'agreement' ? AGREEMENT_CONTENT : POLICY_CONTENT;

  return (
    <div className={`profile-list-overlay${leaving ? ' page-leaving' : ''}`}>
      <div className="profile-list-header">
        <button className="profile-list-back" onClick={handleBack}>← 返回</button>
        <span className="profile-list-title">
          {tab === 'agreement' ? '用户协议' : '隐私政策'}
        </span>
        <span style={{ width: 48 }} />
      </div>

      {/* Tab 切换 */}
      <div style={{
        display: 'flex',
        gap: 0,
        padding: '0 16px',
        borderBottom: '1px solid rgba(255,255,255,0.06)',
      }}>
        <button
          onClick={() => setTab('agreement')}
          style={{
            flex: 1,
            padding: '10px 0',
            background: 'none',
            border: 'none',
            color: tab === 'agreement' ? '#b4a0fa' : 'rgba(255,255,255,0.4)',
            fontSize: 13,
            fontWeight: tab === 'agreement' ? 600 : 400,
            borderBottom: tab === 'agreement' ? '2px solid #b4a0fa' : '2px solid transparent',
            cursor: 'pointer',
            transition: 'all 0.2s',
          }}
        >
          用户协议
        </button>
        <button
          onClick={() => setTab('policy')}
          style={{
            flex: 1,
            padding: '10px 0',
            background: 'none',
            border: 'none',
            color: tab === 'policy' ? '#b4a0fa' : 'rgba(255,255,255,0.4)',
            fontSize: 13,
            fontWeight: tab === 'policy' ? 600 : 400,
            borderBottom: tab === 'policy' ? '2px solid #b4a0fa' : '2px solid transparent',
            cursor: 'pointer',
            transition: 'all 0.2s',
          }}
        >
          隐私政策
        </button>
      </div>

      <div className="profile-list-body">
        <div className="profile-list-scroll">
        <div style={{
          padding: '16px 20px 40px',
          fontSize: 13,
          lineHeight: 1.9,
          color: 'rgba(224,224,240,0.8)',
          whiteSpace: 'pre-wrap',
          wordBreak: 'break-word',
        }}>
          {content.split('\n').map((line, i) => {
            if (line.startsWith('# ')) {
              return <h2 key={i} style={{ fontSize: 18, fontWeight: 600, color: '#b4a0fa', margin: '16px 0 8px', lineHeight: 1.4 }}>{line.replace(/^# /, '')}</h2>;
            }
            if (line.startsWith('## ')) {
              return <h3 key={i} style={{ fontSize: 15, fontWeight: 600, color: '#e0e0f0', margin: '20px 0 6px', lineHeight: 1.4 }}>{line.replace(/^## /, '')}</h3>;
            }
            if (line.startsWith('---')) {
              return <hr key={i} style={{ border: 'none', borderTop: '1px solid rgba(255,255,255,0.06)', margin: '16px 0' }} />;
            }
            if (line.startsWith('- **')) {
              const match = line.match(/^- \*\*(.+?)\*\*：?(.*)$/);
              if (match) {
                return <div key={i} style={{ margin: '4px 0', paddingLeft: 12 }}><span style={{ color: '#67e8f9', fontWeight: 500 }}>{match[1]}</span>{match[2] ? `：${match[2]}` : ''}</div>;
              }
            }
            if (line.startsWith('- ')) {
              return <div key={i} style={{ margin: '4px 0', paddingLeft: 12 }}>• {line.replace(/^- /, '')}</div>;
            }
            if (line.trim() === '') return <div key={i} style={{ height: 8 }} />;
            // Bold text inline
            const parts = line.split(/(\*\*.*?\*\*)/g);
            return (
              <div key={i} style={{ margin: '4px 0' }}>
                {parts.map((part, j) => {
                  if (part.startsWith('**') && part.endsWith('**')) {
                    return <strong key={j} style={{ color: '#e0e0f0' }}>{part.slice(2, -2)}</strong>;
                  }
                  return part;
                })}
              </div>
            );
          })}
        </div>
        </div>
      </div>
    </div>
  );
}
