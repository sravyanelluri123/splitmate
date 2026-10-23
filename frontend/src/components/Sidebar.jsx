import { useState } from 'react'
import { api } from '../api.js'

export default function Sidebar({ groups, selected, onSelect, onCreated }) {
  const [name, setName] = useState('')
  const [members, setMembers] = useState('')
  const [error, setError] = useState('')

  async function create(e) {
    e.preventDefault()
    setError('')
    const names = members.split(',').map((s) => s.trim()).filter(Boolean)
    try {
      const g = await api.createGroup(name.trim(), names)
      setName('')
      setMembers('')
      onCreated(g)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <aside className="sidebar">
      <div className="brand">SplitMate</div>
      <nav aria-label="Groups">
        {groups.length === 0 && <p className="muted">No groups yet.</p>}
        {groups.map((g) => (
          <button key={g.id} className={`group-link ${g.id === selected ? 'active' : ''}`} onClick={() => onSelect(g.id)}>
            <span>{g.name}</span>
            <small>{g.memberCount} people</small>
          </button>
        ))}
      </nav>
      <form className="new-group" onSubmit={create}>
        <h2>New group</h2>
        <label>Group name
          <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Flat 4B" required />
        </label>
        <label>People, separated by commas
          <input value={members} onChange={(e) => setMembers(e.target.value)} placeholder="Asha, Ravi, Meera" />
        </label>
        {error && <p className="error">{error}</p>}
        <button className="primary" type="submit">Create group</button>
      </form>
    </aside>
  )
}
