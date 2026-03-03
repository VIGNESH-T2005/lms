import { useEffect, useMemo, useState } from 'react'
import SectionCard from './components/SectionCard'
import { api } from './services/api'

const emptyStudent = { name: '', email: '' }
const emptyCourse = { title: '', description: '', instructor: '' }
const emptyAssignment = { courseId: '', title: '', description: '', dueDate: '', maxScore: 100 }

export default function App() {
  const [stats, setStats] = useState(null)
  const [students, setStudents] = useState([])
  const [courses, setCourses] = useState([])
  const [enrollments, setEnrollments] = useState([])
  const [assignments, setAssignments] = useState([])
  const [submissions, setSubmissions] = useState([])

  const [studentForm, setStudentForm] = useState(emptyStudent)
  const [courseForm, setCourseForm] = useState(emptyCourse)
  const [assignmentForm, setAssignmentForm] = useState(emptyAssignment)
  const [selectedStudentId, setSelectedStudentId] = useState('')
  const [selectedCourseId, setSelectedCourseId] = useState('')
  const [selectedAssignmentId, setSelectedAssignmentId] = useState('')
  const [submissionStudentId, setSubmissionStudentId] = useState('')
  const [submissionScore, setSubmissionScore] = useState(0)

  const [studentSearch, setStudentSearch] = useState('')
  const [courseSearch, setCourseSearch] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const loadData = async () => {
    setLoading(true)
    try {
      const [
        dashboardStats,
        studentsData,
        coursesData,
        enrollmentsData,
        assignmentsData,
        submissionsData
      ] = await Promise.all([
        api.getDashboardStats(),
        api.getStudents(studentSearch),
        api.getCourses(courseSearch),
        api.getEnrollments(),
        api.getAssignments(),
        api.getSubmissions()
      ])
      setStats(dashboardStats)
      setStudents(studentsData)
      setCourses(coursesData)
      setEnrollments(enrollmentsData)
      setAssignments(assignmentsData)
      setSubmissions(submissionsData)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const refreshWithFilters = async () => {
    try {
      setStudents(await api.getStudents(studentSearch))
      setCourses(await api.getCourses(courseSearch))
    } catch (err) {
      setError(err.message)
    }
  }

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
      await api.createEnrollment({ studentId: Number(selectedStudentId), courseId: Number(selectedCourseId) })
      await loadData()
    } catch (err) {
      setError(err.message)
    }
  }

  const createAssignment = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await api.createAssignment({
        ...assignmentForm,
        courseId: Number(assignmentForm.courseId),
        maxScore: Number(assignmentForm.maxScore)
      })
      setAssignmentForm(emptyAssignment)
      await loadData()
    } catch (err) {
      setError(err.message)
    }
  }

  const createSubmission = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await api.createSubmission({
        assignmentId: Number(selectedAssignmentId),
        studentId: Number(submissionStudentId),
        score: Number(submissionScore)
      })
      await loadData()
    } catch (err) {
      setError(err.message)
    }
  }

  const performanceBoard = useMemo(() => {
    return [...submissions].sort((a, b) => b.score - a.score).slice(0, 5)
  }, [submissions])

  return (
    <main className="container">
      <header className="hero">
        <div>
          <h1>Advanced LMS Control Center</h1>
          <p>Track learner outcomes, manage courses, and monitor performance analytics.</p>
        </div>
        <button onClick={loadData} disabled={loading}>{loading ? 'Refreshing...' : 'Refresh Data'}</button>
      </header>

      {error && <div className="error">{error}</div>}

      {stats && (
        <section className="stats-grid">
          <article><h3>{stats.totalStudents}</h3><p>Students</p></article>
          <article><h3>{stats.totalCourses}</h3><p>Courses</p></article>
          <article><h3>{stats.totalEnrollments}</h3><p>Enrollments</p></article>
          <article><h3>{stats.totalAssignments}</h3><p>Assignments</p></article>
          <article><h3>{stats.totalSubmissions}</h3><p>Submissions</p></article>
          <article><h3>{stats.averageScore}</h3><p>Avg Score</p></article>
        </section>
      )}

      <div className="grid">
        <SectionCard title="Student Management">
          <div className="inline-search">
            <input placeholder="Search student by name/email" value={studentSearch} onChange={(e) => setStudentSearch(e.target.value)} />
            <button onClick={refreshWithFilters}>Apply</button>
          </div>
          <form onSubmit={createStudent} className="form">
            <input placeholder="Student name" value={studentForm.name} onChange={(e) => setStudentForm((s) => ({ ...s, name: e.target.value }))} required />
            <input type="email" placeholder="Student email" value={studentForm.email} onChange={(e) => setStudentForm((s) => ({ ...s, email: e.target.value }))} required />
            <button type="submit">Create Student</button>
          </form>
          <ul>{students.map((student) => <li key={student.id}>{student.name} ({student.email})</li>)}</ul>
        </SectionCard>

        <SectionCard title="Course Management">
          <div className="inline-search">
            <input placeholder="Search course by title/instructor" value={courseSearch} onChange={(e) => setCourseSearch(e.target.value)} />
            <button onClick={refreshWithFilters}>Apply</button>
          </div>
          <form onSubmit={createCourse} className="form">
            <input placeholder="Course title" value={courseForm.title} onChange={(e) => setCourseForm((s) => ({ ...s, title: e.target.value }))} required />
            <input placeholder="Instructor" value={courseForm.instructor} onChange={(e) => setCourseForm((s) => ({ ...s, instructor: e.target.value }))} required />
            <textarea placeholder="Course description" value={courseForm.description} onChange={(e) => setCourseForm((s) => ({ ...s, description: e.target.value }))} required />
            <button type="submit">Create Course</button>
          </form>
          <ul>{courses.map((course) => <li key={course.id}>{course.title} — {course.instructor}</li>)}</ul>
        </SectionCard>

        <SectionCard title="Enrollment">
          <form onSubmit={createEnrollment} className="form">
            <select value={selectedStudentId} onChange={(e) => setSelectedStudentId(e.target.value)} required>
              <option value="">Select student</option>
              {students.map((student) => <option key={student.id} value={student.id}>{student.name}</option>)}
            </select>
            <select value={selectedCourseId} onChange={(e) => setSelectedCourseId(e.target.value)} required>
              <option value="">Select course</option>
              {courses.map((course) => <option key={course.id} value={course.id}>{course.title}</option>)}
            </select>
            <button type="submit">Enroll Student</button>
          </form>
          <ul>{enrollments.map((enrollment) => <li key={enrollment.id}>{enrollment.studentName} → {enrollment.courseTitle}</li>)}</ul>
        </SectionCard>

        <SectionCard title="Assignment Planner">
          <form onSubmit={createAssignment} className="form">
            <select value={assignmentForm.courseId} onChange={(e) => setAssignmentForm((s) => ({ ...s, courseId: e.target.value }))} required>
              <option value="">Select course</option>
              {courses.map((course) => <option key={course.id} value={course.id}>{course.title}</option>)}
            </select>
            <input placeholder="Assignment title" value={assignmentForm.title} onChange={(e) => setAssignmentForm((s) => ({ ...s, title: e.target.value }))} required />
            <textarea placeholder="Assignment description" value={assignmentForm.description} onChange={(e) => setAssignmentForm((s) => ({ ...s, description: e.target.value }))} required />
            <input type="date" value={assignmentForm.dueDate} onChange={(e) => setAssignmentForm((s) => ({ ...s, dueDate: e.target.value }))} required />
            <input type="number" min="1" value={assignmentForm.maxScore} onChange={(e) => setAssignmentForm((s) => ({ ...s, maxScore: e.target.value }))} required />
            <button type="submit">Publish Assignment</button>
          </form>
          <ul>{assignments.map((a) => <li key={a.id}>{a.title} ({a.courseTitle}) • due {a.dueDate}</li>)}</ul>
        </SectionCard>

        <SectionCard title="Grading & Submissions">
          <form onSubmit={createSubmission} className="form">
            <select value={selectedAssignmentId} onChange={(e) => setSelectedAssignmentId(e.target.value)} required>
              <option value="">Select assignment</option>
              {assignments.map((a) => <option key={a.id} value={a.id}>{a.title}</option>)}
            </select>
            <select value={submissionStudentId} onChange={(e) => setSubmissionStudentId(e.target.value)} required>
              <option value="">Select student</option>
              {students.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
            <input type="number" min="0" value={submissionScore} onChange={(e) => setSubmissionScore(e.target.value)} required />
            <button type="submit">Save Grade</button>
          </form>
          <ul>{submissions.map((s) => <li key={s.id}>{s.studentName} - {s.assignmentTitle}: {s.score} ({s.status})</li>)}</ul>
        </SectionCard>

        <SectionCard title="Top Performers">
          <ol>{performanceBoard.map((s) => <li key={s.id}>{s.studentName} — {s.score} in {s.assignmentTitle}</li>)}</ol>
        </SectionCard>
      </div>
    </main>
  )
}
