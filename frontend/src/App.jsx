import { useCallback, useEffect, useState } from 'react'
import { api } from './api.js'
import Sidebar from './components/Sidebar.jsx'
import GroupView from './components/GroupView.jsx'

export default function App() {
  const [groups, setGroups] = useState([])
  const [selected, setSelected] = useState(null)
  const [error, setError] = useState('')

  const loadGroups = useCallback(async () => {
    try {
      setGroups(await api.groups())
      setError('')
    } catch (e) {
      setError(`Can't reach the server. Is the backend running on port 8080? (${e.message})`)
    }
  }, [])

  useEffect(() => { loadGroups() }, [loadGroups])

  return (
    <div className="shell">
      <Sidebar
        groups={groups}
        selected={selected}
        onSelect={setSelected}
        onCreated={async (g) => { await loadGroups(); setSelected(g.id) }}
      />
      <main className="main">
        {error && <p className="banner error" role="alert">{error}</p>}
        {selected
          ? <GroupView key={selected} groupId={selected} onChanged={loadGroups} />
          : <div className="empty">
              <h1>Split what you share</h1>
              <p>Create a group for your trip or household, then add the first expense.</p>
            </div>}
      </main>
    </div>
  )
}
