import { useEffect, useState } from 'react'
import SectionCard from './components/SectionCard'
import { api } from './services/api'

const emptyStudent = { name: '', email: '' }
const emptyCourse = { title: '', description: '', instructor: '' }

export default function App() {
  const [students, setStudents] = useState([])
  const [courses, setCourses] = useState([])
  const [enrollments, setEnrollments] = useState([])
  const [studentForm, setStudentForm] = useState(emptyStudent)
  const [courseForm, setCourseForm] = useState(emptyCourse)
  const [selectedStudentId, setSelectedStudentId] = useState('')
  const [selectedCourseId, setSelectedCourseId] = useState('')
  const [error, setError] = useState('')

  const loadData = async () => {
    try {
      const [studentsData, coursesData, enrollmentsData] = await Promise.all([
        api.getStudents(),
        api.getCourses(),
        api.getEnrollments()
      ])
      setStudents(studentsData)
      setCourses(coursesData)
      setEnrollments(enrollmentsData)
    } catch (err) {
      setError(err.message)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const createStudent = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await api.createStudent(studentForm)
      setStudentForm(emptyStudent)
      await loadData()
    } catch (err) {
      setError(err.message)
    }
  }

  const createCourse = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await api.createCourse(courseForm)
      setCourseForm(emptyCourse)
      await loadData()
    } catch (err) {
      setError(err.message)
    }
  }

  const createEnrollment = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await api.createEnrollment({
        studentId: Number(selectedStudentId),
        courseId: Number(selectedCourseId)
      })
      await loadData()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <main className="container">
      <header>
        <h1>Learning Management System</h1>
        <p>Manage students, courses, and enrollments in one place.</p>
      </header>

      {error && <div className="error">{error}</div>}

      <div className="grid">
        <SectionCard title="Students">
          <form onSubmit={createStudent} className="form">
            <input
              placeholder="Student name"
              value={studentForm.name}
              onChange={(e) => setStudentForm((s) => ({ ...s, name: e.target.value }))}
              required
            />
            <input
              type="email"
              placeholder="Student email"
              value={studentForm.email}
              onChange={(e) => setStudentForm((s) => ({ ...s, email: e.target.value }))}
              required
            />
            <button type="submit">Add Student</button>
          </form>
          <ul>
            {students.map((student) => (
              <li key={student.id}>{student.name} ({student.email})</li>
            ))}
          </ul>
        </SectionCard>

        <SectionCard title="Courses">
          <form onSubmit={createCourse} className="form">
            <input
              placeholder="Course title"
              value={courseForm.title}
              onChange={(e) => setCourseForm((s) => ({ ...s, title: e.target.value }))}
              required
            />
            <input
              placeholder="Instructor"
              value={courseForm.instructor}
              onChange={(e) => setCourseForm((s) => ({ ...s, instructor: e.target.value }))}
              required
            />
            <textarea
              placeholder="Course description"
              value={courseForm.description}
              onChange={(e) => setCourseForm((s) => ({ ...s, description: e.target.value }))}
              required
            />
            <button type="submit">Add Course</button>
          </form>
          <ul>
            {courses.map((course) => (
              <li key={course.id}>{course.title} — {course.instructor}</li>
            ))}
          </ul>
        </SectionCard>

        <SectionCard title="Enrollments">
          <form onSubmit={createEnrollment} className="form">
            <select value={selectedStudentId} onChange={(e) => setSelectedStudentId(e.target.value)} required>
              <option value="">Select student</option>
              {students.map((student) => (
                <option key={student.id} value={student.id}>{student.name}</option>
              ))}
            </select>
            <select value={selectedCourseId} onChange={(e) => setSelectedCourseId(e.target.value)} required>
              <option value="">Select course</option>
              {courses.map((course) => (
                <option key={course.id} value={course.id}>{course.title}</option>
              ))}
            </select>
            <button type="submit">Enroll</button>
          </form>
          <ul>
            {enrollments.map((enrollment) => (
              <li key={enrollment.id}>
                {enrollment.studentName} → {enrollment.courseTitle} ({enrollment.status})
              </li>
            ))}
          </ul>
        </SectionCard>
      </div>
    </main>
  )
}
