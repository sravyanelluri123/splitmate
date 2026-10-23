async function request(path, options = {}) {
  const res = await fetch(`/api${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
    body: options.body ? JSON.stringify(options.body) : undefined,
  })
  if (res.status === 204) return null
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(data.error || `Request failed (${res.status})`)
  return data
}

export const api = {
  groups: () => request('/groups'),
  createGroup: (name, members) => request('/groups', { method: 'POST', body: { name, members } }),
  group: (id) => request(`/groups/${id}`),
  addMember: (id, name) => request(`/groups/${id}/members`, { method: 'POST', body: { name } }),
  expenses: (id) => request(`/groups/${id}/expenses`),
  addExpense: (id, body) => request(`/groups/${id}/expenses`, { method: 'POST', body }),
  deleteExpense: (id, expenseId) => request(`/groups/${id}/expenses/${expenseId}`, { method: 'DELETE' }),
  payments: (id) => request(`/groups/${id}/payments`),
  recordPayment: (id, body) => request(`/groups/${id}/payments`, { method: 'POST', body }),
  settlement: (id) => request(`/groups/${id}/settlement`),
}

export const money = (n) =>
  new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(Number(n))
