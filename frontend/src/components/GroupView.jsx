import { useCallback, useEffect, useState } from 'react'
import { api } from '../api.js'
import Balances from './Balances.jsx'
import ExpenseForm from './ExpenseForm.jsx'
import History from './History.jsx'

export default function GroupView({ groupId, onChanged }) {
  const [group, setGroup] = useState(null)
  const [expenses, setExpenses] = useState([])
  const [payments, setPayments] = useState([])
  const [settlement, setSettlement] = useState(null)
  const [error, setError] = useState('')
  const [newMember, setNewMember] = useState('')

  const refresh = useCallback(async () => {
    try {
      const [g, e, p, s] = await Promise.all([
        api.group(groupId), api.expenses(groupId), api.payments(groupId), api.settlement(groupId),
      ])
      setGroup(g); setExpenses(e); setPayments(p); setSettlement(s); setError('')
    } catch (err) {
      setError(err.message)
    }
  }, [groupId])

  useEffect(() => { refresh() }, [refresh])

  async function addMember(e) {
    e.preventDefault()
    try {
      await api.addMember(groupId, newMember.trim())
      setNewMember('')
      await refresh()
      onChanged()
    } catch (err) { setError(err.message) }
  }

  async function markPaid(t) {
    try {
      await api.recordPayment(groupId, { fromId: t.fromId, toId: t.toId, amount: t.amount })
      await refresh()
    } catch (err) { setError(err.message) }
  }

  async function removeExpense(id) {
    try {
      await api.deleteExpense(groupId, id)
      await refresh()
    } catch (err) { setError(err.message) }
  }

  if (!group) return <p className="muted">{error || 'Loading…'}</p>

  return (
    <div className="group">
      <header className="group-head">
        <h1>{group.name}</h1>
        <form className="inline" onSubmit={addMember}>
          <input value={newMember} onChange={(e) => setNewMember(e.target.value)} placeholder="Add a person" aria-label="Add a person" required />
          <button type="submit">Add</button>
        </form>
      </header>
      {error && <p className="banner error" role="alert">{error}</p>}

      <Balances settlement={settlement} onMarkPaid={markPaid} />

      <div className="two-col">
        <ExpenseForm group={group} onAdded={refresh} />
        <History expenses={expenses} payments={payments} onDelete={removeExpense} />
      </div>
    </div>
  )
}
