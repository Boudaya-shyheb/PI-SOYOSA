import { Component, Input, Output, EventEmitter, ElementRef, ViewChild } from '@angular/core';

@Component({
  selector: 'app-certificate',
  templateUrl: './certificate.component.html',
  styleUrls: ['./certificate.component.css']
})
export class CertificateComponent {
  @Input() userName = 'Name Surname';
  @Input() courseName = 'Course Title';
  
  @Output() close = new EventEmitter<void>();

  @ViewChild('certificateEl') certificateEl!: ElementRef;

  currentDate = new Date().toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });

  onClose(): void {
    this.close.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('certificate-modal')) {
      this.onClose();
    }
  }

  downloadCertificate(): void {
    // Simple print-based download approach
    const printWindow = window.open('', '_blank');
    if (!printWindow) {
      alert('Please allow popups to download the certificate');
      return;
    }

    const certificateHtml = `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Certificate - ${this.courseName}</title>
        <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }
          body {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background: #f5f5f5;
            font-family: 'Georgia', serif;
          }
          .certificate {
            width: 800px;
            height: 560px;
            background: #fff;
            position: relative;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
          }
          .wave-top {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 120px;
            background: #2D5757;
            clip-path: polygon(0 0, 100% 0, 100% 60%, 85% 80%, 70% 60%, 55% 80%, 40% 60%, 25% 80%, 10% 60%, 0 80%);
          }
          .wave-bottom {
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 120px;
            background: #2D5757;
            clip-path: polygon(0 40%, 15% 20%, 30% 40%, 45% 20%, 60% 40%, 75% 20%, 90% 40%, 100% 20%, 100% 100%, 0 100%);
          }
          .gold-border {
            position: absolute;
            top: 20px;
            left: 20px;
            right: 20px;
            bottom: 20px;
            border: 2px solid #C9A84C;
          }
          .inner-border {
            position: absolute;
            top: 30px;
            left: 30px;
            right: 30px;
            bottom: 30px;
            border: 1px solid #C9A84C;
          }
          .content {
            position: absolute;
            top: 120px;
            left: 60px;
            right: 60px;
            bottom: 140px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            text-align: center;
          }
          .logo {
            width: 50px;
            height: 50px;
            border: 2px solid #2D5757;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 10px;
          }
          .title {
            font-size: 48px;
            color: #2D5757;
            font-weight: bold;
            margin-bottom: 5px;
          }
          .subtitle {
            font-size: 12px;
            color: #888;
            letter-spacing: 4px;
            text-transform: uppercase;
            margin-bottom: 20px;
          }
          .presented {
            font-size: 10px;
            color: #888;
            letter-spacing: 2px;
            text-transform: uppercase;
            margin-bottom: 10px;
          }
          .recipient {
            font-size: 36px;
            color: #333;
            font-style: italic;
            margin-bottom: 20px;
            border-bottom: 1px solid #C9A84C;
            padding-bottom: 5px;
          }
          .description {
            font-size: 11px;
            color: #666;
            line-height: 1.6;
            max-width: 500px;
            margin-bottom: 30px;
          }
          .signatures {
            display: flex;
            justify-content: space-between;
            width: 100%;
            max-width: 400px;
          }
          .signature {
            text-align: center;
          }
          .signature-line {
            width: 120px;
            border-top: 1px solid #333;
            margin-bottom: 5px;
          }
          .signature-label {
            font-size: 10px;
            color: #666;
            text-transform: uppercase;
          }
          .seal {
            position: absolute;
            left: 50px;
            top: 50%;
            transform: translateY(-50%);
            width: 80px;
            height: 80px;
            background: #2D5757;
            border-radius: 50%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #C9A84C;
            font-size: 10px;
            font-weight: bold;
            text-transform: uppercase;
          }
          .award {
            position: absolute;
            right: 30px;
            top: 30px;
            width: 80px;
            height: 80px;
            background: #C9A84C;
            border-radius: 50%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-weight: bold;
          }
          .award-text-top {
            font-size: 14px;
          }
          .award-text-bottom {
            font-size: 10px;
          }
          @media print {
            body { background: white; }
            .certificate { box-shadow: none; }
          }
        </style>
      </head>
      <body>
        <div class="certificate">
          <div class="wave-top"></div>
          <div class="wave-bottom"></div>
          <div class="gold-border"></div>
          <div class="inner-border"></div>
          
          <div class="seal">
            <span>Verified</span>
            <span>Seal</span>
          </div>
          
          <div class="award">
            <span class="award-text-top">Official</span>
            <span class="award-text-bottom">Certificate</span>
          </div>
          
          <div class="content">
            <div style="display:flex; align-items:center; justify-content:center; gap:10px; margin-bottom:10px;">
              <div class="logo" style="margin-bottom:0;">○</div>
              <div style="font-size:14px; font-weight:bold; color:#2D5757; letter-spacing:1px;">JUNGLE IN ENGLISH</div>
            </div>
            <div class="title">Certificate</div>
            <div class="subtitle">of Appreciation</div>
            <div class="presented">Proudly Presented To</div>
            <div class="recipient">${this.userName}</div>
            <div class="description">
              This certifies that the student has successfully completed the required coursework for "${this.courseName}". 
              This certificate is awarded in recognition of the dedication, commitment, and excellence demonstrated throughout the learning journey at Jungle In English.
            </div>
            <div class="signatures">
              <div class="signature">
                <div class="signature-line"></div>
                <div class="signature-label">Course Instructor</div>
              </div>
              <div class="signature">
                <div class="signature-line"></div>
                <div class="signature-label">Platform Director</div>
              </div>
            </div>
          </div>
        </div>
        <script>
          window.onload = function() {
            window.print();
          }
        </script>
      </body>
      </html>
    `;

    printWindow.document.write(certificateHtml);
    printWindow.document.close();
  }
}
