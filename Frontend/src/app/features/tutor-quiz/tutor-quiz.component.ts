import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { Router } from '@angular/router';
import { QuizService } from '../../services/quiz.service';
import { QuizApiService } from '../../services/quiz-api.service';
import { QuizAttemptService } from '../../services/quiz-attempt.service';
import { Quiz, QuizQuestion } from '../../models/quiz-models';
import { QuizAttempt } from '../../models/assessment-models';
import { AuthService } from '../../services/auth.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export type QuizView = 'dashboard' | 'builder' | 'preview';
export type BuilderStep = 'settings' | 'questions' | 'theme' | 'preview';
export type QuestionType = 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'FILL_BLANK' | 'MEDIA';

export interface QuizTheme {
  name: string;
  id: string;
  primaryColor: string;
  secondaryColor: string;
  bgGradient: string;
  emoji: string;
}

export interface QuizStats {
  quizId: string;
  attempts: number;
  avgScore: number;
  passRate: number;
}

export interface TutorQuizDraft extends Partial<Quiz> {
  theme?: string;
  customPrimary?: string;
  customBg?: string;
  customFont?: string;
  xpReward?: number;
  badge?: string;
  animationSpeed?: 'slow' | 'normal' | 'fast';
  darkMode?: boolean;
  shuffleQuestions?: boolean;
  shuffleAnswers?: boolean;
  showExplanations?: boolean;
  // Internal helper for stable option tracking
  _editorQuestions?: any[];
}

@Component({
  selector: 'app-tutor-quiz',
  templateUrl: './tutor-quiz.component.html',
  styleUrls: ['./tutor-quiz.component.css']
})
export class TutorQuizComponent implements OnInit, OnDestroy {
  @Input() isFinalQuizMode = false;
  @Input() courseId = '';

  view: QuizView = 'dashboard';
  builderStep: BuilderStep = 'settings';

  // Dashboard state
  quizList: TutorQuizDraft[] = [];
  filteredQuizList: TutorQuizDraft[] = [];
  searchTerm = '';
  loadingDashboard = true;
  statsMap: Record<string, QuizStats> = {};
  darkMode = false;

  // Builder state
  editingQuiz: TutorQuizDraft | null = null;
  savingQuiz = false;
  saveError = '';
  saveSuccess = '';

  // Drag state for question reorder
  dragSourceIndex: number | null = null;
  dragOverIndex: number | null = null;

  // Preview state
  previewQuestionIndex = 0;
  previewAnswer: any = null;
  previewSubmitted = false;

  readonly THEMES: QuizTheme[] = [
    { id: 'ocean',   name: 'Ocean',   primaryColor: '#0ea5e9', secondaryColor: '#0284c7', bgGradient: 'linear-gradient(135deg,#0f172a,#0c4a6e)', emoji: '🌊' },
    { id: 'forest',  name: 'Forest',  primaryColor: '#22c55e', secondaryColor: '#16a34a', bgGradient: 'linear-gradient(135deg,#052e16,#166534)', emoji: '🌿' },
    { id: 'galaxy',  name: 'Galaxy',  primaryColor: '#a855f7', secondaryColor: '#7e22ce', bgGradient: 'linear-gradient(135deg,#0f0a1e,#4a1d96)', emoji: '🪐' },
    { id: 'candy',   name: 'Candy',   primaryColor: '#f43f5e', secondaryColor: '#e11d48', bgGradient: 'linear-gradient(135deg,#4a0018,#be123c)', emoji: '🍭' },
    { id: 'minimal', name: 'Minimal', primaryColor: '#2D5757', secondaryColor: '#1a3c3c', bgGradient: 'linear-gradient(135deg,#f8fafc,#e2e8f0)', emoji: '⬜' },
  ];

  readonly BADGES = ['🏆 Champion', '⭐ Star', '🎯 Sharpshooter', '🔥 On Fire', '💎 Diamond', '🚀 Rocket', '🦅 Eagle'];
  readonly FONTS = ['Inter', 'Poppins', 'Roboto', 'Merriweather', 'Playfair Display'];
  readonly QUESTION_TYPES: { value: QuestionType; label: string; icon: string }[] = [
    { value: 'MULTIPLE_CHOICE', label: 'Multiple Choice', icon: '☑️' },
    { value: 'TRUE_FALSE',      label: 'True / False',    icon: '⚖️' },
    { value: 'FILL_BLANK',      label: 'Fill in the Blank', icon: '✏️' },
    { value: 'MEDIA',           label: 'Media (Image/Video)', icon: '🎬' },
  ];

  // Stats for the SVG charts — computed after load
  totalQuizzes = 0;
  totalAttempts = 0;
  avgPassRate = 0;

  constructor(
    private quizService: QuizService,
    private quizApi: QuizApiService,
    private quizAttempts: QuizAttemptService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (this.isFinalQuizMode) {
      this.view = 'builder';
      this.loadFinalQuizForCourse();
    } else {
      this.loadDashboard();
    }
  }

  ngOnDestroy(): void {}

  private loadFinalQuizForCourse(): void {
    this.loadingDashboard = true;
    this.quizApi.getQuizzesByCourse(this.courseId).subscribe(quizzes => {
      const finalQuiz = quizzes.find(q => q.title.toLowerCase().includes('final'));
      if (finalQuiz) {
        this.editQuiz(finalQuiz);
      } else {
        this.createNewFinalQuiz();
      }
      this.loadingDashboard = false;
    });
  }

  private createNewFinalQuiz(): void {
    this.editingQuiz = {
      title: 'Final Assessment',
      courseId: this.courseId,
      timeLimitMin: 60,
      passingScore: 75,
      maxAttempts: 3,
      cooldownMin: 1440, // 24 hours
      questions: [],
      theme: 'galaxy',
      _editorQuestions: []
    };
    this.view = 'builder';
    this.builderStep = 'settings';
  }

  // ─── Dashboard ───────────────────────────────────────────────

  loadDashboard(): void {
    this.loadingDashboard = true;
    // We load all cached quizzes and attempt results
    this.quizAttempts.getMyResults().pipe(catchError(() => of([]))).subscribe(attempts => {
      // Group attempts by quizId
      const byQuiz: Record<string, QuizAttempt[]> = {};
      attempts.forEach(a => {
        if (!a.quizId) return;
        if (!byQuiz[a.quizId]) byQuiz[a.quizId] = [];
        byQuiz[a.quizId].push(a);
      });

      // Pull quizzes from the API (all cached) — since there's no "list all quizzes" endpoint for tutor
      // we rely on cache; for the demo we create some mock entries if none found.
      const cached = Array.from((this.quizService as any).cache?.values() ?? []) as Quiz[];
      this.quizList = cached.map(q => ({ ...q, theme: 'minimal', xpReward: 100, badge: '🏆 Champion', shuffleQuestions: true, shuffleAnswers: true, showExplanations: true, animationSpeed: 'normal', darkMode: false }));

      // Compute stats
      this.statsMap = {};
      this.quizList.forEach(q => {
        if (!q.id) return;
        const att = byQuiz[q.id] ?? [];
        const passing = q.passingScore ?? 70;
        const avg = att.length ? Math.round(att.reduce((s, a) => s + (a.score ?? 0), 0) / att.length) : 0;
        const passRate = att.length ? Math.round(att.filter(a => (a.score ?? 0) >= passing).length / att.length * 100) : 0;
        this.statsMap[q.id] = { quizId: q.id, attempts: att.length, avgScore: avg, passRate };
      });

      this.totalQuizzes = this.quizList.length;
      this.totalAttempts = attempts.length;
      const rates = Object.values(this.statsMap).map(s => s.passRate);
      this.avgPassRate = rates.length ? Math.round(rates.reduce((a, b) => a + b, 0) / rates.length) : 0;

      this.applySearch();
      this.loadingDashboard = false;
    });
  }

  applySearch(): void {
    const term = this.searchTerm.toLowerCase();
    this.filteredQuizList = this.quizList.filter(q => (q.title ?? '').toLowerCase().includes(term));
  }

  onSearchChange(): void {
    this.applySearch();
  }

  // ─── Quiz Lifecycle ───────────────────────────────────────────

  editQuiz(quiz: TutorQuizDraft): void {
    this.editingQuiz = JSON.parse(JSON.stringify(quiz)); // Deep copy
    this.prepareQuestionsForEditor();
    this.view = 'builder';
    this.builderStep = 'settings';
  }

  createNewQuiz(): void {
    this.editingQuiz = {
      title: 'New Quiz',
      timeLimitMin: 30,
      passingScore: 80,
      maxAttempts: 1,
      cooldownMin: 0,
      questions: [],
      theme: 'minimal',
      _editorQuestions: []
    };
    this.view = 'builder';
    this.builderStep = 'settings';
  }

  /** Convert string options to objects for stable ngModel tracking */
  prepareQuestionsForEditor(): void {
    if (!this.editingQuiz) return;
    this.editingQuiz._editorQuestions = (this.editingQuiz.questions ?? []).map(q => ({
      ...q,
      // Create a stable reference for ngFor trackBy
      _editorOptions: (q.options ?? []).map(opt => ({ id: Math.random(), text: opt }))
    }));
  }

  cloneQuiz(quiz: TutorQuizDraft): void {
    const clone = JSON.parse(JSON.stringify(quiz)) as TutorQuizDraft;
    clone.id = `draft-${Date.now()}`;
    clone.title = (clone.title ?? 'Quiz') + ' (Copy)';
    clone.createdAt = new Date().toISOString();
    clone.updatedAt = new Date().toISOString();
    this.quizList.unshift(clone);
    this.applySearch();
  }

  deleteQuiz(quiz: TutorQuizDraft, event: Event): void {
    event.stopPropagation();
    if (!confirm(`Delete "${quiz.title}"?`)) return;
    if (quiz.lessonId) {
      this.quizService.deleteQuiz(quiz.lessonId).subscribe(() => {
        this.quizList = this.quizList.filter(q => q.id !== quiz.id);
        this.applySearch();
      });
    } else {
      this.quizList = this.quizList.filter(q => q.id !== quiz.id);
      this.applySearch();
    }
  }

  saveQuiz(): void {
    if (!this.editingQuiz) return;
    this.savingQuiz = true;
    this.saveError = '';
    this.saveSuccess = '';

    // Convert editor questions back to the API format
    this.editingQuiz.questions = this.getQuestionsFromEditor();

    const quizData: any = {
      ...this.editingQuiz,
      courseId: this.isFinalQuizMode ? this.courseId : this.editingQuiz.courseId,
      lessonId: this.isFinalQuizMode ? null : this.editingQuiz.lessonId,
    };

    // Remove internal editor state
    delete quizData._editorQuestions;

    const saveObservable = this.editingQuiz.id
      ? this.quizApi.updateQuiz(this.editingQuiz.id, quizData)
      : this.quizApi.createQuiz(quizData);

    saveObservable.pipe(
      catchError(err => {
        this.saveError = `Failed to save quiz: ${err.error?.message || err.message}`;
        this.savingQuiz = false;
        return of(null);
      })
    ).subscribe(savedQuiz => {
      if (savedQuiz) {
        this.savingQuiz = false;
        this.saveSuccess = 'Quiz saved successfully!';
        this.editingQuiz = { ...this.editingQuiz, ...savedQuiz };
        this.prepareQuestionsForEditor();

        if (this.isFinalQuizMode) {
          // In final quiz mode, we just show success and stay
          setTimeout(() => this.saveSuccess = '', 3000);
        } else {
          // In normal mode, go back to dashboard after a delay
          setTimeout(() => {
            this.view = 'dashboard';
            this.loadDashboard();
          }, 1500);
        }
      }
    });
  }

  cancelBuilder(): void {
    this.editingQuiz = null;
    this.view = 'dashboard';
  }

  // ─── Builder Steps ────────────────────────────────────────────

  goStep(step: BuilderStep): void {
    this.builderStep = step;
    if (step === 'preview') this.resetPreview();
  }

  steps: BuilderStep[] = ['settings', 'questions', 'theme', 'preview'];
  stepIndex(): number { return this.steps.indexOf(this.builderStep); }
  nextStep(): void { const i = this.stepIndex(); if (i < this.steps.length - 1) this.goStep(this.steps[i + 1]); }
  prevStep(): void { const i = this.stepIndex(); if (i > 0) this.goStep(this.steps[i - 1]); }

  // ─── Questions Editor ─────────────────────────────────────────

  newQuestion(type: QuestionType = 'MULTIPLE_CHOICE'): any {
    return {
      id: `q-${Math.random().toString(36).slice(2, 8)}`,
      text: '',
      type,
      _editorOptions: type === 'TRUE_FALSE' 
        ? [{ id: 't', text: 'True' }, { id: 'f', text: 'False' }] 
        : [
            { id: 'a', text: 'Option A' },
            { id: 'b', text: 'Option B' },
            { id: 'c', text: 'Option C' },
            { id: 'd', text: 'Option D' }
          ],
      correctIndex: 0,
      correctAnswer: '',
      explanation: '',
      mediaUrl: '',
      mediaType: 'IMAGE',
    };
  }

  addQuestion(type: QuestionType = 'MULTIPLE_CHOICE'): void {
    if (!this.editingQuiz) return;
    if (!this.editingQuiz._editorQuestions) this.editingQuiz._editorQuestions = [];
    this.editingQuiz._editorQuestions.push(this.newQuestion(type));
  }

  removeQuestion(index: number): void {
    if (!this.editingQuiz?._editorQuestions) return;
    if (this.editingQuiz._editorQuestions.length <= 1) return;
    this.editingQuiz._editorQuestions.splice(index, 1);
  }

  changeQuestionType(qIndex: number, type: QuestionType): void {
    if (!this.editingQuiz?._editorQuestions) return;
    const q = this.editingQuiz._editorQuestions[qIndex];
    q.type = type;
    if (type === 'TRUE_FALSE') {
      q._editorOptions = [{ id: 't', text: 'True' }, { id: 'f', text: 'False' }];
      q.correctIndex = 0;
    } else if (type === 'FILL_BLANK' || type === 'MEDIA') {
      q._editorOptions = [];
      q.correctIndex = undefined;
    } else {
      if (!q._editorOptions?.length) {
        q._editorOptions = [
          { id: 'a', text: 'Option A' },
          { id: 'b', text: 'Option B' },
          { id: 'c', text: 'Option C' },
          { id: 'd', text: 'Option D' }
        ];
      }
      q.correctIndex = 0;
    }
  }

  addOption(qIndex: number): void {
    const q = this.editingQuiz?._editorQuestions?.[qIndex];
    if (!q || !q._editorOptions) return;
    q._editorOptions.push({
      id: `opt-${Math.random().toString(36).slice(2, 9)}`,
      text: `Option ${q._editorOptions.length + 1}`
    });
  }

  removeOption(qIndex: number, oIndex: number): void {
    const q = this.editingQuiz?._editorQuestions?.[qIndex];
    if (!q?._editorOptions || q._editorOptions.length <= 2) return;
    q._editorOptions.splice(oIndex, 1);
    if ((q.correctIndex ?? 0) >= q._editorOptions.length) q.correctIndex = 0;
  }

  trackByQuestion(i: number, q: any): string { return q.id || i.toString(); }
  trackByOption(i: number, o: any): string { return o.id || i.toString(); }
  trackByQuiz(i: number, q: any): string { return q.id || i.toString(); }

  // ─── Drag & Drop (question reorder) ──────────────────────────

  onDragStart(index: number): void { this.dragSourceIndex = index; }
  onDragOver(event: DragEvent, index: number): void { event.preventDefault(); this.dragOverIndex = index; }
  onDrop(index: number): void {
    if (this.dragSourceIndex === null || !this.editingQuiz?._editorQuestions) return;
    const qs = this.editingQuiz._editorQuestions;
    const [moved] = qs.splice(this.dragSourceIndex, 1);
    qs.splice(index, 0, moved);
    this.dragSourceIndex = null;
    this.dragOverIndex = null;
  }
  onDragEnd(): void { this.dragSourceIndex = null; this.dragOverIndex = null; }

  // ─── Theme ────────────────────────────────────────────────────

  getTheme(id: string): QuizTheme {
    return this.THEMES.find(t => t.id === id) ?? this.THEMES[4];
  }

  selectTheme(id: string): void {
    if (!this.editingQuiz) return;
    this.editingQuiz.theme = id;
    const t = this.getTheme(id);
    this.editingQuiz.customPrimary = t.primaryColor;
  }

  // ─── Preview ─────────────────────────────────────────────────

  resetPreview(): void {
    this.previewQuestionIndex = 0;
    this.previewAnswer = null;
    this.previewSubmitted = false;
  }

  previewNext(): void {
    const len = this.editingQuiz?._editorQuestions?.length ?? 0;
    if (this.previewQuestionIndex < len - 1) {
      this.previewQuestionIndex++;
      this.previewAnswer = null;
      this.previewSubmitted = false;
    }
  }

  previewPrev(): void {
    if (this.previewQuestionIndex > 0) {
      this.previewQuestionIndex--;
      this.previewAnswer = null;
      this.previewSubmitted = false;
    }
  }

  previewSubmitAnswer(): void { this.previewSubmitted = true; }

  previewCurrentQuestion(): any | null {
    return this.editingQuiz?._editorQuestions?.[this.previewQuestionIndex] ?? null;
  }

  previewIsCorrect(): boolean {
    const q = this.previewCurrentQuestion();
    if (!q) return false;
    if (q.type === 'FILL_BLANK' || q.type === 'MEDIA') {
      return q.correctAnswer?.toLowerCase().trim() === (this.previewAnswer ?? '').toString().toLowerCase().trim();
    }
    // Options are objects in editor, but indices are still used for correctness
    return q.correctIndex === this.previewAnswer;
  }

  // ─── Helpers ─────────────────────────────────────────────────

  /** Bar height for SVG mini chart, 0–50 */
  chartBarH(val: number, max = 100): number { return Math.round((val / max) * 50); }

  /** Dynamic CSS variables for the current quiz theme */
  themeStyle(): Record<string, string> {
    if (!this.editingQuiz) return {};
    const t = this.getTheme(this.editingQuiz.theme ?? 'minimal');
    return {
      '--q-primary': this.editingQuiz.customPrimary ?? t.primaryColor,
      '--q-secondary': t.secondaryColor,
      '--q-bg': t.bgGradient,
      '--q-font': this.editingQuiz.customFont ?? 'Inter',
    };
  }

  letterLabel(i: number): string { return ['A', 'B', 'C', 'D', 'E', 'F'][i] ?? String(i + 1); }

  isManager(): boolean { return this.auth.isAdmin() || this.auth.isTeacher(); }

  // ─── Question Management ─────────────────────────────────────

  getQuestionsFromEditor(): QuizQuestion[] {
    if (!this.editingQuiz?._editorQuestions) {
      return [];
    }
    return this.editingQuiz._editorQuestions.map(editorQ => {
      const newQ: QuizQuestion = {
        id: editorQ.id,
        text: editorQ.text,
        type: editorQ.type,
        options: (editorQ._editorOptions ?? []).map((opt: any) => opt.text),
        correctIndex: editorQ.correctIndex,
        explanation: editorQ.explanation,
        mediaUrl: editorQ.mediaUrl,
        timeLimitSec: editorQ.timeLimitSec,
        points: editorQ.points,
      };
      return newQ;
    });
  }
}
