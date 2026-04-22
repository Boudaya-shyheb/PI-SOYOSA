import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BlogService } from '../../../services/blog.service';
import {CommentService} from "../../../services/comment.service";
import {AuthService} from "../../../services/auth/auth.service";
import Swal from 'sweetalert2';



@Component({
  selector: 'app-blog-detail',
  templateUrl: './blog-detail.component.html',
  styleUrl: './blog-detail.component.css'
})
export class BlogDetailComponent implements OnInit {
    newComment={
        content:"",
         blog:{
             blogId:0,
         },
         userId:""
    }
  comments: any;
  blog:any;
    likedComments: { [key: number]: boolean } = {};
    currentUserEmail: string | null = null;
    editingCommentId: number | null = null;
    editingContent: string = "";
    openDropdownId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private blogService: BlogService,
    private commentService: CommentService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
      this.currentUserEmail = this.authService.getUserEmail();
      const id = this.route.snapshot.paramMap.get('id');
      console.log(id);
      if (id) {
          this.blogService.getBlogById(id).subscribe((data: any) => {
              this.blog = data;
              // Initialize likedBlog status using checkLike service
              if (this.currentUserEmail) {
                  this.blogService.checkLike(this.blog.blogId, this.currentUserEmail).subscribe((res: any) => {
                      this.likedBlog = res;
                  });
              }
          });
          this.loadAllComments(id);
      }
  }

    likedBlog=false;

    toggleLike(): void {
        if (!this.blog) return;

        if (!this.authService.isLoggedIn()) {
            Swal.fire({
                title: 'Authentication Required',
                text: 'You must be logged in to like a blog.',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Login',
                cancelButtonText: 'OK'
            }).then((result) => {
                if (result.isConfirmed) {
                    this.authService.logout(); // This navigates to login
                }
            });
            return;
        }

        if (!this.likedBlog) {
            this.likedBlog = true;
            this.blogService.likeBlog(this.blog.blogId, this.currentUserEmail).subscribe({
                next: (updatedBlog) => {
                    this.blog.likes = updatedBlog.likes;
                    this.blog.likedByUsers = updatedBlog.likedByUsers;
                },
                error: (err) => {
                    console.error(err);
                }
            });
        }else{
            this.likedBlog = false;
            this.blogService.dislikeBlog(this.blog.blogId,this.currentUserEmail).subscribe({
                next: (updatedBlog) => {
                    this.blog.likes = updatedBlog.likes;
                    this.blog.likedByUsers = updatedBlog.likedByUsers;
                },
                error: (err) => {
                    console.error(err);
                }
            });
        }

    }

    addComment(): void {
        if (!this.blog) return;

        if (!this.authService.isLoggedIn()) {
            Swal.fire({
                title: 'Authentication Required',
                text: 'You must be logged in to add a comment.',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Login',
                cancelButtonText: 'OK'
            }).then((result) => {
                if (result.isConfirmed) {
                    this.authService.logout();
                }
            });
            return;
        }

        if (!this.newComment.content.trim()) return;

        this.newComment.blog.blogId=this.blog.blogId;
        this.newComment.userId=this.authService.getUserEmail() || "Anonymous";


        this.commentService.createComment(this.newComment)
            .subscribe({
                next: (savedComment: any) => {
                    this.loadAllComments(this.blog.blogId);
                    this.newComment.content="";
                },
                error: (err) => {
                    console.error(err);
                    if (err.error && (err.error.message || typeof err.error === 'string')) {
                        const errorMsg = typeof err.error === 'string' ? err.error : err.error.message;
                        if (errorMsg.includes("inappropriate language")) {
                            Swal.fire({
                                title: 'Inappropriate Content',
                                text: 'Your comment contains bad words. Please remove them and try again.',
                                icon: 'error',
                                confirmButtonText: 'OK'
                            });
                        }
                    }
                }
            });
    }

    updateComment(comment: any) {
        this.commentService.updateComment(comment.commentId, comment)
            .subscribe({
                next: (updated) => {
                    this.loadAllComments(this.blog.blogId);
                    this.cancelEdit();
                },
                error: (err) => {
                    console.error(err);
                    if (err.error && (err.error.message || typeof err.error === 'string')) {
                        const errorMsg = typeof err.error === 'string' ? err.error : err.error.message;
                        if (errorMsg.includes("inappropriate language")) {
                            Swal.fire({
                                title: 'Inappropriate Content',
                                text: 'Your comment contains bad words. Please remove them and try again.',
                                icon: 'error',
                                confirmButtonText: 'OK'
                            });
                        }
                    }
                }
            });
    }

    deleteComment(id: number) {
        Swal.fire({
            title: 'Are you sure?',
            text: "You won't be able to revert this!",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, delete it!'
        }).then((result) => {
            if (result.isConfirmed) {
                this.commentService.deleteComment(id).subscribe(() => {
                    this.loadAllComments(this.blog.blogId);
                    Swal.fire(
                        'Deleted!',
                        'Your comment has been deleted.',
                        'success'
                    );
                });
            }
        });
    }

    reportComment(comment: any) {
        Swal.fire({
            title: 'Report Comment',
            input: 'textarea',
            inputLabel: 'Reason for reporting',
            inputPlaceholder: 'Type your reason here...',
            inputAttributes: {
                'aria-label': 'Type your reason here'
            },
            showCancelButton: true,
            confirmButtonText: 'Submit Report',
            showLoaderOnConfirm: true,
            preConfirm: (reason) => {
                if (!reason) {
                    Swal.showValidationMessage('Please provide a reason');
                    return false;
                }
                return this.commentService.reportComment(comment.commentId, reason).toPromise()
                    .then(response => {
                        return response;
                    })
                    .catch(error => {
                        Swal.showValidationMessage(`Request failed: ${error}`);
                    });
            },
            allowOutsideClick: () => !Swal.isLoading()
        }).then((result) => {
            if (result.isConfirmed) {
                Swal.fire(
                    'Reported!',
                    'Comment reported. Thank you for your feedback.',
                    'success'
                );
            }
        });
    }

    toggleDropdown(id: number) {
        this.openDropdownId = this.openDropdownId === id ? null : id;
    }

    startEdit(comment: any) {
        this.editingCommentId = comment.commentId;
        this.editingContent = comment.content;
    }

    cancelEdit() {
        this.editingCommentId = null;
        this.editingContent = "";
    }

    saveEdit(comment: any) {
        if (!this.editingContent.trim()) return;
        const updatedComment = { ...comment, content: this.editingContent };
        this.updateComment(updatedComment);
    }



    loadAllComments(id: any) {
        this.commentService.getCommentsByBlog(id)
            .subscribe((comments: any[]) => {
                this.comments = comments;
                // Initialize liked status for each comment using checkLike service
                if (this.currentUserEmail) {
                    comments.forEach(comment => {
                        this.commentService.checkLike(comment.commentId, this.currentUserEmail).subscribe((res: any) => {
                            this.likedComments[comment.commentId] = res;
                        });
                    });
                }
            });
    }

    toggleCommentLike(comment: any) {
        if (!this.authService.isLoggedIn()) {
            Swal.fire({
                title: 'Authentication Required',
                text: 'You must be logged in to like a comment.',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Login',
                cancelButtonText: 'OK'
            }).then((result) => {
                if (result.isConfirmed) {
                    this.authService.logout();
                }
            });
            return;
        }

        const alreadyLiked = this.likedComments[comment.commentId];

        if (!alreadyLiked) {
            this.commentService.likeComment(comment.commentId, this.currentUserEmail)
                .subscribe({
                    next: (updatedComment: any) => {
                        comment.likes = updatedComment.likes;
                        comment.likedByUsers = updatedComment.likedByUsers;
                        this.likedComments[comment.commentId] = true;
                    },
                    error: (err) => console.error(err)
                });
        } else {
            this.commentService.dislikeComment(comment.commentId, this.currentUserEmail)
                .subscribe({
                    next: (updatedComment: any) => {
                        comment.likes = updatedComment.likes;
                        comment.likedByUsers = updatedComment.likedByUsers;
                        this.likedComments[comment.commentId] = false;
                    },
                    error: (err) => console.error(err)
                });
        }
    }


}
