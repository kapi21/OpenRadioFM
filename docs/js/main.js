document.addEventListener('DOMContentLoaded', () => {
    // Generate Waveform Bars
    const waveformContainer = document.getElementById('waveform');
    const barCount = 40;

    for (let i = 0; i < barCount; i++) {
        const bar = document.createElement('div');
        bar.className = 'wave-bar';
        waveformContainer.appendChild(bar);
    }

    // Animation function for the waveform
    const animateWaveform = () => {
        const bars = document.querySelectorAll('.wave-bar');
        bars.forEach((bar, index) => {
            const height = Math.random() * 50 + 10;
            const opacity = Math.random() * 0.5 + 0.5;
            bar.style.height = `${height}%`;
            bar.style.opacity = opacity;
        });
    };

    // Waveform Styles injected via JS
    const style = document.createElement('style');
    style.textContent = `
        #waveform {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 3px;
            height: 60px;
        }
        .wave-bar {
            width: 4px;
            height: 20%;
            background: var(--primary);
            border-radius: 2px;
            transition: all 0.15s ease-in-out;
            box-shadow: 0 0 5px var(--primary-glow);
        }
    `;
    document.head.appendChild(style);

    setInterval(animateWaveform, 150);

    // Dynamic Frequency Scanning Effect
    const dialElement = document.querySelector('.dial-number');
    if (dialElement) {
        let baseFreq = 93.50;
        setInterval(() => {
            // Small random variation to simulate scanning/fine-tuning
            const variation = (Math.random() * 0.1 - 0.05);
            baseFreq += variation;
            if (baseFreq < 87.5) baseFreq = 108.0;
            if (baseFreq > 108.0) baseFreq = 87.5;
            dialElement.textContent = baseFreq.toFixed(2);
        }, 1000);
    }

    // Scroll reveal logic
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
    revealOnScroll(); // Initial check
});
