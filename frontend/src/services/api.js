const API_BASE_URL = 'http://localhost:8080/api'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    ...options
  })

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || 'Request failed')
  }

  return response.json()
}

export const api = {
  getStudents: () => request('/students'),
  createStudent: (payload) => request('/students', { method: 'POST', body: JSON.stringify(payload) }),
  getCourses: () => request('/courses'),
  createCourse: (payload) => request('/courses', { method: 'POST', body: JSON.stringify(payload) }),
  getEnrollments: () => request('/enrollments'),
  createEnrollment: (payload) => request('/enrollments', { method: 'POST', body: JSON.stringify(payload) })
}
