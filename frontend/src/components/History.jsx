import { money } from '../api.js'

const LABEL = { EQUAL: 'split equally', PERCENTAGE: 'split by percent', CUSTOM: 'split by amounts' }

export default function History({ expenses, payments, onDelete }) {
  const items = [
    ...expenses.map((e) => ({ kind: 'expense', key: `e${e.id}`, sort: e.date + String(e.id).padStart(10, '0'), e })),
    ...payments.map((p) => ({ kind: 'payment', key: `p${p.id}`, sort: p.createdAt.slice(0, 10) + String(p.id).padStart(10, '0'), p })),
  ].sort((a, b) => (a.sort < b.sort ? 1 : -1))

  return (
    <section className="panel" aria-labelledby="hist-h">
      <h2 id="hist-h">History</h2>
      {items.length === 0 && <p className="muted">Expenses and repayments will show up here.</p>}
      <ul className="history">
        {items.map((it) =>
          it.kind === 'expense' ? (
            <li key={it.key}>
              <div className="line">
                <strong>{it.e.description}</strong>
                <span className="num">{money(it.e.amount)}</span>
              </div>
              <p className="muted">
                {it.e.paidByName} paid · {LABEL[it.e.splitType]} · {it.e.date}
              </p>
              <p className="shares">
                {it.e.shares.map((s) => `${s.memberName} ${money(s.amount)}`).join(', ')}
              </p>
              <button className="link danger" onClick={() => onDelete(it.e.id)}>Delete</button>
            </li>
          ) : (
            <li key={it.key} className="payment">
              <div className="line">
                <span><strong>{it.p.fromName}</strong> paid <strong>{it.p.toName}</strong></span>
                <span className="num">{money(it.p.amount)}</span>
              </div>
              <p className="muted">Repayment · {it.p.createdAt.slice(0, 10)}</p>
            </li>
          ),
        )}
      </ul>
    </section>
  )
}
