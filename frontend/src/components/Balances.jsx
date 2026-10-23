import { money } from '../api.js'

export default function Balances({ settlement, onMarkPaid }) {
  if (!settlement) return null
  const { balances, transfers } = settlement
  const max = Math.max(1, ...balances.map((b) => Math.abs(Number(b.net))))

  return (
    <section className="panel" aria-labelledby="bal-h">
      <h2 id="bal-h">Where everyone stands</h2>
      <ul className="bars">
        {balances.map((b) => {
          const n = Number(b.net)
          const pct = (Math.abs(n) / max) * 100
          const state = n > 0 ? 'owed' : n < 0 ? 'owes' : 'even'
          return (
            <li key={b.memberId} className={`bar-row ${state}`}>
              <span className="who">{b.name}</span>
              <span className="track" aria-hidden="true">
                <span className="axis" />
                <span className="fill" style={{ width: `${pct / 2}%` }} />
              </span>
              <span className="amt">
                {state === 'even' ? 'All square' : state === 'owed' ? `Gets back ${money(n)}` : `Owes ${money(-n)}`}
              </span>
            </li>
          )
        })}
      </ul>

      <h3>Settle up</h3>
      {transfers.length === 0
        ? <p className="muted">Nobody owes anyone right now.</p>
        : <ul className="transfers">
            {transfers.map((t) => (
              <li key={`${t.fromId}-${t.toId}`}>
                <span><strong>{t.fromName}</strong> pays <strong>{t.toName}</strong></span>
                <span className="num">{money(t.amount)}</span>
                <button onClick={() => onMarkPaid(t)}>Mark as paid</button>
              </li>
            ))}
          </ul>}
    </section>
  )
}
