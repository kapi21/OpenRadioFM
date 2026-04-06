document.addEventListener('DOMContentLoaded', () => {
    const sections = document.querySelectorAll('section');
    const revealOnScroll = () => {
        sections.forEach(section => {
            const rect = section.getBoundingClientRect();
            if (rect.top < window.innerHeight * 0.8) {
                section.style.opacity = '1';
                section.style.transform = 'translateY(0)';
            }
        });
    };

    sections.forEach(s => {
        s.style.opacity = '0';
        s.style.transform = 'translateY(30px)';
        s.style.transition = 'all 0.6s ease-out';
    });

    window.addEventListener('scroll', revealOnScroll);
    revealOnScroll();
});
