import { useEffect, useMemo, useState } from 'react'
import { api } from './services/api'
import SectionCard from './components/SectionCard'

const initialForms = {
  login: { email: 'admin@lms.com', password: 'Admin@123' },
  student: { name: '', email: '' },
  course: { title: '', description: '', instructor: '' },
  enrollment: { studentId: '', courseId: '' },
  assignment: { courseId: '', title: '', description: '', dueDate: '', maxScore: 100 },
  submission: { assignmentId: '', studentId: '', score: 0 },
  attendance: { studentId: '', courseId: '', attendanceDate: '', status: 'PRESENT', notes: '' },
  certification: { studentId: '', courseId: '', finalScore: 80, remarks: '' }
}

export default function App() {
  const [forms, setForms] = useState(initialForms)
  const [me, setMe] = useState(null)
  const [stats, setStats] = useState(null)
  const [students, setStudents] = useState([])
  const [courses, setCourses] = useState([])
  const [enrollments, setEnrollments] = useState([])
  const [assignments, setAssignments] = useState([])
  const [submissions, setSubmissions] = useState([])
  const [attendance, setAttendance] = useState([])
  const [certifications, setCertifications] = useState([])
  const [search, setSearch] = useState({ student: '', course: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const updateForm = (scope, key, value) => {
    setForms((prev) => ({ ...prev, [scope]: { ...prev[scope], [key]: value } }))
  }

  const loadAll = async () => {
    setLoading(true)
    try {
      const [
        user,
        dashboard,
        studentsData,
        coursesData,
        enrollmentsData,
        assignmentsData,
        submissionsData,
        attendanceData,
        certificationsData
      ] = await Promise.all([
        api.me(),
        api.getDashboardStats(),
        api.getStudents(search.student),
        api.getCourses(search.course),
        api.getEnrollments(),
        api.getAssignments(),
        api.getSubmissions(),
        api.getAttendance(),
        api.getCertifications()
      ])
      setMe(user)
      setStats(dashboard)
      setStudents(studentsData)
      setCourses(coursesData)
      setEnrollments(enrollmentsData)
      setAssignments(assignmentsData)
      setSubmissions(submissionsData)
      setAttendance(attendanceData)
      setCertifications(certificationsData)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadAll()
  }, [])

  const runAction = async (action, resetScope) => {
    setError('')
    try {
      await action()
      if (resetScope) {
        setForms((prev) => ({ ...prev, [resetScope]: initialForms[resetScope] }))
      }
      await loadAll()
    } catch (err) {
      setError(err.message)
    }
  }

  const handleLogin = (e) => {
    e.preventDefault()
    runAction(async () => {
      const result = await api.login(forms.login)
      api.setToken(result.token)
    })
  }

  const attendanceRateColor = useMemo(() => {
    const rate = stats?.attendanceRate || 0
    if (rate >= 85) return 'good'
    if (rate >= 60) return 'warn'
    return 'bad'
  }, [stats])

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <h2>Central LMS</h2>
        <p>Unified portal for access, academics, attendance, and certification.</p>
        <form onSubmit={handleLogin} className="form compact">
          <h3>Sign In</h3>
          <input type="email" value={forms.login.email} onChange={(e) => updateForm('login', 'email', e.target.value)} required />
          <input type="password" value={forms.login.password} onChange={(e) => updateForm('login', 'password', e.target.value)} required />
          <button type="submit">Login</button>
        </form>
        {me && <div className="user-chip">{me.fullName} · {me.role}</div>}
      </aside>

      <section className="content">
        <header className="header">
          <div>
            <h1>Learning Intelligence Dashboard</h1>
            <p>One modern workspace for enrollment, assessments, attendance, and certifications.</p>
          </div>
          <button onClick={loadAll} disabled={loading}>{loading ? 'Syncing...' : 'Sync Data'}</button>
        </header>

        {error && <div className="error-banner">{error}</div>}

        {stats && (
          <section className="stats-grid">
            <article><h3>{stats.totalUsers}</h3><p>Users</p></article>
            <article><h3>{stats.totalStudents}</h3><p>Learners</p></article>
            <article><h3>{stats.totalCourses}</h3><p>Courses</p></article>
            <article><h3>{stats.totalEnrollments}</h3><p>Enrollments</p></article>
            <article><h3>{stats.totalAssignments}</h3><p>Assessments</p></article>
            <article><h3>{stats.totalSubmissions}</h3><p>Submissions</p></article>
            <article><h3>{stats.totalAttendanceRecords}</h3><p>Attendance Logs</p></article>
            <article><h3>{stats.totalCertifications}</h3><p>Certificates</p></article>
            <article><h3>{stats.averageScore}</h3><p>Average Score</p></article>
            <article className={attendanceRateColor}><h3>{stats.attendanceRate}%</h3><p>Attendance Rate</p></article>
          </section>
        )}

        <div className="module-grid">
          <SectionCard title="Learners">
            <div className="inline-row">
              <input placeholder="Search learners" value={search.student} onChange={(e) => setSearch((s) => ({ ...s, student: e.target.value }))} />
              <button onClick={loadAll}>Find</button>
            </div>
            <form className="form" onSubmit={(e) => { e.preventDefault(); runAction(() => api.createStudent(forms.student), 'student') }}>
              <input placeholder="Name" value={forms.student.name} onChange={(e) => updateForm('student', 'name', e.target.value)} required />
              <input placeholder="Email" type="email" value={forms.student.email} onChange={(e) => updateForm('student', 'email', e.target.value)} required />
              <button>Create Learner</button>
            </form>
            <ul>{students.map((s) => <li key={s.id}>{s.name} <span>{s.email}</span></li>)}</ul>
          </SectionCard>

          <SectionCard title="Courses & Enrollment">
            <div className="inline-row">
              <input placeholder="Search courses" value={search.course} onChange={(e) => setSearch((s) => ({ ...s, course: e.target.value }))} />
              <button onClick={loadAll}>Find</button>
            </div>
            <form className="form" onSubmit={(e) => { e.preventDefault(); runAction(() => api.createCourse(forms.course), 'course') }}>
              <input placeholder="Course title" value={forms.course.title} onChange={(e) => updateForm('course', 'title', e.target.value)} required />
              <input placeholder="Instructor" value={forms.course.instructor} onChange={(e) => updateForm('course', 'instructor', e.target.value)} required />
              <textarea placeholder="Description" value={forms.course.description} onChange={(e) => updateForm('course', 'description', e.target.value)} required />
              <button>Create Course</button>
            </form>
            <form className="form" onSubmit={(e) => { e.preventDefault(); runAction(() => api.createEnrollment({ studentId: Number(forms.enrollment.studentId), courseId: Number(forms.enrollment.courseId) }), 'enrollment') }}>
              <select value={forms.enrollment.studentId} onChange={(e) => updateForm('enrollment', 'studentId', e.target.value)} required>
                <option value="">Select learner</option>
                {students.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
              <select value={forms.enrollment.courseId} onChange={(e) => updateForm('enrollment', 'courseId', e.target.value)} required>
                <option value="">Select course</option>
                {courses.map((c) => <option key={c.id} value={c.id}>{c.title}</option>)}
              </select>
              <button>Enroll</button>
            </form>
            <ul>{enrollments.map((en) => <li key={en.id}>{en.studentName} → {en.courseTitle}</li>)}</ul>
          </SectionCard>

          <SectionCard title="Assessments">
            <form className="form" onSubmit={(e) => { e.preventDefault(); runAction(() => api.createAssignment({ ...forms.assignment, courseId: Number(forms.assignment.courseId), maxScore: Number(forms.assignment.maxScore) }), 'assignment') }}>
              <select value={forms.assignment.courseId} onChange={(e) => updateForm('assignment', 'courseId', e.target.value)} required>
                <option value="">Course</option>
                {courses.map((c) => <option key={c.id} value={c.id}>{c.title}</option>)}
              </select>
              <input placeholder="Assessment title" value={forms.assignment.title} onChange={(e) => updateForm('assignment', 'title', e.target.value)} required />
              <textarea placeholder="Assessment details" value={forms.assignment.description} onChange={(e) => updateForm('assignment', 'description', e.target.value)} required />
              <input type="date" value={forms.assignment.dueDate} onChange={(e) => updateForm('assignment', 'dueDate', e.target.value)} required />
              <input type="number" min="1" value={forms.assignment.maxScore} onChange={(e) => updateForm('assignment', 'maxScore', e.target.value)} required />
              <button>Publish Assessment</button>
            </form>
            <form className="form" onSubmit={(e) => { e.preventDefault(); runAction(() => api.createSubmission({ assignmentId: Number(forms.submission.assignmentId), studentId: Number(forms.submission.studentId), score: Number(forms.submission.score) }), 'submission') }}>
              <select value={forms.submission.assignmentId} onChange={(e) => updateForm('submission', 'assignmentId', e.target.value)} required>
                <option value="">Assessment</option>
                {assignments.map((a) => <option key={a.id} value={a.id}>{a.title}</option>)}
              </select>
              <select value={forms.submission.studentId} onChange={(e) => updateForm('submission', 'studentId', e.target.value)} required>
                <option value="">Learner</option>
                {students.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
              <input type="number" min="0" value={forms.submission.score} onChange={(e) => updateForm('submission', 'score', e.target.value)} required />
              <button>Submit Grade</button>
            </form>
            <ul>{submissions.map((s) => <li key={s.id}>{s.studentName} · {s.assignmentTitle} · {s.score} ({s.status})</li>)}</ul>
          </SectionCard>

          <SectionCard title="Attendance & Certifications">
            <form className="form" onSubmit={(e) => { e.preventDefault(); runAction(() => api.createAttendance({ ...forms.attendance, studentId: Number(forms.attendance.studentId), courseId: Number(forms.attendance.courseId) }), 'attendance') }}>
              <select value={forms.attendance.studentId} onChange={(e) => updateForm('attendance', 'studentId', e.target.value)} required>
                <option value="">Learner</option>
                {students.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
              <select value={forms.attendance.courseId} onChange={(e) => updateForm('attendance', 'courseId', e.target.value)} required>
                <option value="">Course</option>
                {courses.map((c) => <option key={c.id} value={c.id}>{c.title}</option>)}
              </select>
              <input type="date" value={forms.attendance.attendanceDate} onChange={(e) => updateForm('attendance', 'attendanceDate', e.target.value)} required />
              <select value={forms.attendance.status} onChange={(e) => updateForm('attendance', 'status', e.target.value)}>
                <option value="PRESENT">PRESENT</option>
                <option value="ABSENT">ABSENT</option>
                <option value="LATE">LATE</option>
              </select>
              <input placeholder="Notes" value={forms.attendance.notes} onChange={(e) => updateForm('attendance', 'notes', e.target.value)} />
              <button>Mark Attendance</button>
            </form>
            <form className="form" onSubmit={(e) => { e.preventDefault(); runAction(() => api.createCertification({ ...forms.certification, studentId: Number(forms.certification.studentId), courseId: Number(forms.certification.courseId), finalScore: Number(forms.certification.finalScore) }), 'certification') }}>
              <select value={forms.certification.studentId} onChange={(e) => updateForm('certification', 'studentId', e.target.value)} required>
                <option value="">Learner</option>
                {students.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
              <select value={forms.certification.courseId} onChange={(e) => updateForm('certification', 'courseId', e.target.value)} required>
                <option value="">Course</option>
                {courses.map((c) => <option key={c.id} value={c.id}>{c.title}</option>)}
              </select>
              <input type="number" min="60" value={forms.certification.finalScore} onChange={(e) => updateForm('certification', 'finalScore', e.target.value)} required />
              <input placeholder="Remarks" value={forms.certification.remarks} onChange={(e) => updateForm('certification', 'remarks', e.target.value)} />
              <button>Issue Certificate</button>
            </form>
            <ul>
              {attendance.slice(0, 4).map((a) => <li key={`a-${a.id}`}>{a.studentName} · {a.courseTitle} · {a.status}</li>)}
              {certifications.slice(0, 4).map((c) => <li key={`c-${c.id}`}>{c.studentName} · {c.courseTitle} · {c.certificateCode}</li>)}
            </ul>
          </SectionCard>
        </div>
      </section>
    </main>
  )
}
