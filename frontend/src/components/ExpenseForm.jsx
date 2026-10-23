import { useEffect, useMemo, useState } from 'react'
import { api, money } from '../api.js'

const TYPES = [
  { id: 'EQUAL', label: 'Equally' },
  { id: 'PERCENTAGE', label: 'By percent' },
  { id: 'CUSTOM', label: 'Exact amounts' },
]

export default function ExpenseForm({ group, onAdded }) {
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [paidById, setPaidById] = useState(group.members[0]?.id ?? '')
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))
  const [type, setType] = useState('EQUAL')
  const [included, setIncluded] = useState({})
  const [values, setValues] = useState({})
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  // Everyone is in the split by default; keep that true when people are added.
  useEffect(() => {
    setIncluded((prev) => {
      const next = { ...prev }
      group.members.forEach((m) => { if (!(m.id in next)) next[m.id] = true })
      return next
    })
    if (!paidById && group.members[0]) setPaidById(group.members[0].id)
  }, [group.members]) // eslint-disable-line react-hooks/exhaustive-deps

  const participants = group.members.filter((m) => included[m.id])
  const total = Number(amount) || 0

  const hint = useMemo(() => {
    if (type === 'PERCENTAGE') {
      const sum = participants.reduce((s, m) => s + (Number(values[m.id]) || 0), 0)
      return { ok: Math.abs(sum - 100) < 0.0005, text: `${+sum.toFixed(3)}% of 100%` }
    }
    if (type === 'CUSTOM') {
      const sum = participants.reduce((s, m) => s + (Number(values[m.id]) || 0), 0)
      const left = Math.round((total - sum) * 100) / 100
      return { ok: left === 0 && total > 0, text: left === 0 ? 'Adds up' : `${money(Math.abs(left))} ${left > 0 ? 'left to assign' : 'over'}` }
    }
    return null
  }, [type, participants, values, total])

  async function submit(e) {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      await api.addExpense(group.id, {
        description, amount, paidById: Number(paidById), splitType: type, date,
        shares: participants.map((m) => ({
          memberId: m.id,
          value: type === 'EQUAL' ? null : values[m.id] === '' || values[m.id] == null ? null : values[m.id],
        })),
      })
      setDescription(''); setAmount(''); setValues({})
      onAdded()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="panel expense-form" onSubmit={submit}>
      <h2>Add an expense</h2>
      <label>What was it for
        <input value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Groceries" required />
      </label>
      <div className="row">
        <label>Amount
          <input type="number" min="0.01" step="0.01" inputMode="decimal" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="0.00" required />
        </label>
        <label>Date
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
        </label>
      </div>
      <label>Paid by
        <select value={paidById} onChange={(e) => setPaidById(e.target.value)} required>
          {group.members.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
        </select>
      </label>

      <fieldset>
        <legend>Split</legend>
        <div className="segmented" role="radiogroup" aria-label="Split method">
          {TYPES.map((t) => (
            <button type="button" key={t.id} role="radio" aria-checked={type === t.id}
              className={type === t.id ? 'on' : ''} onClick={() => setType(t.id)}>{t.label}</button>
          ))}
        </div>

        <ul className="people">
          {group.members.map((m) => (
            <li key={m.id}>
              <label className="check">
                <input type="checkbox" checked={!!included[m.id]}
                  onChange={(e) => setIncluded({ ...included, [m.id]: e.target.checked })} />
                {m.name}
              </label>
              {type !== 'EQUAL' && included[m.id] && (
                <span className="value">
                  <input type="number" min="0" step={type === 'PERCENTAGE' ? '0.01' : '0.01'} inputMode="decimal"
                    aria-label={`${m.name} ${type === 'PERCENTAGE' ? 'percent' : 'amount'}`}
                    value={values[m.id] ?? ''} onChange={(e) => setValues({ ...values, [m.id]: e.target.value })} />
                  <small>{type === 'PERCENTAGE' ? '%' : '$'}</small>
                </span>
              )}
              {type === 'EQUAL' && included[m.id] && participants.length > 0 && total > 0 && (
                <span className="num muted">≈ {money(total / participants.length)}</span>
              )}
            </li>
          ))}
        </ul>
        {hint && <p className={`hint ${hint.ok ? 'ok' : ''}`}>{hint.text}</p>}
      </fieldset>

      {error && <p className="error" role="alert">{error}</p>}
      <button className="primary" type="submit" disabled={busy || participants.length === 0}>Save expense</button>
    </form>
  )
}
