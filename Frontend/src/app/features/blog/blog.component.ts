import { Component } from '@angular/core';
import {BlogService} from "../../services/blog.service";
import {AuthService} from "../../services/auth/auth.service";
import Swal from "sweetalert2";


@Component({
  selector: 'app-blog',
  templateUrl: './blog.component.html',
  styleUrl: './blog.component.css'
})
export class BlogComponent {

    selectedFiles: File[] = [];
    imagePreviews: string[] = [];

    blogs: any[] = [];
    currentPage = 0;
    pageSize = 6;
    totalPages = 0;
    searchTerm = '';
    sortOption = 'newest';

    blog={
        title:"",
        content:"",
        userId:"",
        images : [],
    };

    constructor( private blogService: BlogService, public authService: AuthService) { }

    ngOnInit(): void {
        this.loadBlogs(true);
    }

    loadBlogs(reset: boolean = false) {

        if (reset) {
            this.currentPage = 0;
            this.blogs = [];
        }

        this.blogService
            .getBlogs(this.currentPage, this.pageSize, this.searchTerm, this.sortOption)
            .subscribe({
                next: (response: any) => {

                    this.totalPages = response.totalPages;

                    // append blogs (for Load More)
                    this.blogs = [...this.blogs, ...response.content];

                    console.log(this.blogs);
                },
                error: (error) => {
                    console.error('Error loading blogs:', error);
                }
            });
    }

    loadMore() {
        if (this.currentPage < this.totalPages - 1) {
            this.currentPage++;
            this.loadBlogs();
        }
    }

    onSearchChange() {
        this.loadBlogs(true);
    }

    onSortChange() {
        this.loadBlogs(true);
    }

    isModalOpen = false;

    openModal() {
        this.isModalOpen = true;
    }

    closeModal() {
        this.isModalOpen = false;
    }

    submitBlog() {

        if (!this.authService.isLoggedIn()) {
            Swal.fire({
                title: 'Authentication Required',
                text: 'You must be logged in to add a blog.',
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

        const formData = new FormData();

        formData.append('title', this.blog.title);
        formData.append('content', this.blog.content);
        formData.append('userId', sessionStorage.getItem("username")!);

        this.selectedFiles.forEach(file => {
            formData.append('images', file);
        });

        this.blogService.createBlog(formData).subscribe({
            next: () => {

                // 1️⃣ Close modal
                this.closeModal();

                // 2️⃣ Reset form
                this.blog = {
                    userId: sessionStorage.getItem("username")!,
                    title: '',
                    content: '',
                    images: []
                };

                this.selectedFiles = [];
                this.imagePreviews = [];

                // 3️⃣ Reset pagination + reload feed
                this.currentPage = 0;
                this.blogs = [];
                this.loadBlogs(true);

            },
            error: (err) => {
                console.error(err);
                if (err.error && (err.error.message || typeof err.error === 'string')) {
                    const errorMsg = typeof err.error === 'string' ? err.error : err.error.message;
                    if (errorMsg.includes("inappropriate language")) {
                        Swal.fire({
                            title: 'Inappropriate Content',
                            text: 'Your blog contains bad words. Please remove them and try again.',
                            icon: 'error',
                            confirmButtonText: 'OK'
                        });
                    }
                }
            }
        });
    }



    onImageSelected(event: any) {
        const files = Array.from(event.target.files) as File[];

        if (this.selectedFiles.length + files.length > 3) {
            alert("You can upload maximum 3 images.");
            return;
        }

        files.forEach(file => {
            this.selectedFiles.push(file);

            const reader = new FileReader();
            reader.onload = () => {
                this.imagePreviews.push(reader.result as string);
            };
            reader.readAsDataURL(file);
        });
    }

    removeImage(index: number) {
        this.selectedFiles.splice(index, 1);
        this.imagePreviews.splice(index, 1);
    }



}
