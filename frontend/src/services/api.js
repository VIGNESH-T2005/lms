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
    let message = 'Request failed'
    try {
      const body = await response.json()
      message = body.message || message
    } catch {
      message = await response.text()
    }
    throw new Error(message || 'Request failed')
  }

  return response.json()
}

export const api = {
  getDashboardStats: () => request('/dashboard/stats'),
  getStudents: (q = '') => request(`/students${q ? `?q=${encodeURIComponent(q)}` : ''}`),
  createStudent: (payload) => request('/students', { method: 'POST', body: JSON.stringify(payload) }),
  getCourses: (q = '') => request(`/courses${q ? `?q=${encodeURIComponent(q)}` : ''}`),
  createCourse: (payload) => request('/courses', { method: 'POST', body: JSON.stringify(payload) }),
  getEnrollments: () => request('/enrollments'),
  createEnrollment: (payload) => request('/enrollments', { method: 'POST', body: JSON.stringify(payload) }),
  getAssignments: (courseId) => request(`/assignments${courseId ? `?courseId=${courseId}` : ''}`),
  createAssignment: (payload) => request('/assignments', { method: 'POST', body: JSON.stringify(payload) }),
  getSubmissions: () => request('/submissions'),
  createSubmission: (payload) => request('/submissions', { method: 'POST', body: JSON.stringify(payload) })
}
