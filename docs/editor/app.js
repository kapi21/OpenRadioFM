let currentData = {
    presets: [],
    version: "1.1",
    timestamp: ""
};

let currentLang = 'es';

const translations = {
    es: {
        pageTitle: "Gestión de favoritos",
        emptyState: "Carga un archivo para empezar",
        dropTitle: "Arrastra tu archivo .fav aquí",
        dropText: "o haz click para seleccionar",
        totalPresets: "Total de Presets:",
        fileLoaded: "Archivo:",
        btnNew: "Nuevo Preset",
        btnSave: "Guardar nuevo .fav",
        modalTitle: "Editar Preset",
        labelName: "Nombre (RDS PS - Máx 8)",
        labelFreq: "Frecuencia",
        helpFreq: "Pon el número como lo ves en la radio (87.6 para FM o 531 para AM)",
        labelBand: "Banda",
        labelSlot: "Slot (Preset #)",
        btnCancel: "Cancelar",
        btnSaveChange: "Guardar Cambios",
        assign: "Asignar",
        empty: "Vacío",
        noName: "Sin Nombre",
        confirmDelete: "¿Eliminar esta emisora?",
        successSave: "Archivo generado correctamente y listo para descargar.",
        invalidFile: "Formato no válido",
        errorRead: "Error al leer el archivo: ",
        invalidFreq: "Por favor introduce una frecuencia válida",
        newPresetTitle: "Nuevo Preset"
    },
    en: {
        pageTitle: "Favorites Management",
        emptyState: "Upload a file to start",
        dropTitle: "Drag your .fav file here",
        dropText: "or click to select",
        totalPresets: "Total Presets:",
        fileLoaded: "File:",
        btnNew: "New Preset",
        btnSave: "Save new .fav",
        modalTitle: "Edit Preset",
        labelName: "Name (RDS PS - Max 8)",
        labelFreq: "Frequency",
        helpFreq: "Enter the value as seen on the radio (87.6 for FM or 531 for AM)",
        labelBand: "Band",
        labelSlot: "Slot (Preset #)",
        btnCancel: "Cancel",
        btnSaveChange: "Save Changes",
        assign: "Assign",
        empty: "Empty",
        noName: "No Name",
        confirmDelete: "Delete this station?",
        successSave: "File generated successfully and ready for download.",
        invalidFile: "Invalid format",
        errorRead: "Error reading file: ",
        invalidFreq: "Please enter a valid frequency",
        newPresetTitle: "New Preset"
    }
};

const elements = {
    dropZone: document.getElementById('dropZone'),
    fileInput: document.getElementById('fileInput'),
    presetGrid: document.getElementById('presetGrid'),
    editorArea: document.getElementById('editorArea'),
    emptyState: document.getElementById('emptyState'),
    totalPresets: document.getElementById('totalPresets'),
    fileNameDisplay: document.getElementById('fileNameDisplay'),
    modal: document.getElementById('modal'),
    presetForm: document.getElementById('presetForm'),
    saveBtn: document.getElementById('saveBtn'),
    addBtn: document.getElementById('addBtn'),
    cancelBtn: document.getElementById('cancelBtn')
};

function setLanguage(lang) {
    currentLang = lang;
    localStorage.setItem('openradio_lang', lang);
    
    // Update active button UI
    document.querySelectorAll('.lang-btn').forEach(btn => btn.classList.remove('active'));
    document.getElementById(`lang-${lang}`).classList.add('active');
    
    // Update all static text
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (translations[lang][key]) {
            // Preservation of internal icons/structures if needed
            const hasIcon = el.querySelector('i[data-lucide]');
            if (hasIcon) {
                const iconHtml = hasIcon.outerHTML;
                el.innerHTML = `${iconHtml} ${translations[lang][key]}`;
            } else {
                el.textContent = translations[lang][key];
            }
        }
    });

    // Re-render dynamic content
    if (currentData.presets.length > 0) render();
}

// Init language
const savedLang = localStorage.getItem('openradio_lang') || (navigator.language.startsWith('en') ? 'en' : 'es');
setTimeout(() => setLanguage(savedLang), 10);

// --- Initialization ---

elements.dropZone.addEventListener('click', () => elements.fileInput.click());
elements.fileInput.addEventListener('change', (e) => handleFile(e.target.files[0]));

elements.dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    elements.dropZone.classList.add('dragover');
});

elements.dropZone.addEventListener('dragleave', () => {
    elements.dropZone.classList.remove('dragover');
});

elements.dropZone.addEventListener('drop', (e) => {
    e.preventDefault();
    elements.dropZone.classList.remove('dragover');
    handleFile(e.dataTransfer.files[0]);
});

// --- Logic functions ---

function handleFile(file) {
    if (!file) return;
    
    const reader = new FileReader();
    reader.onload = (e) => {
        try {
            const data = JSON.parse(e.target.result);
            if (!data.presets) throw new Error(translations[currentLang].invalidFile);
            currentData = data;
            elements.fileNameDisplay.textContent = file.name;
            render();
            showEditor();
        } catch (err) {
            alert(translations[currentLang].errorRead + err.message);
        }
    };
    reader.readAsText(file);
}

function showEditor() {
    elements.dropZone.style.display = 'none';
    elements.editorArea.style.display = 'block';
    elements.emptyState.style.display = 'none';
}

function render() {
    elements.presetGrid.innerHTML = '';
    elements.totalPresets.textContent = currentData.presets.length;

    const bands = [
        { id: 0, name: 'FM1' },
        { id: 1, name: 'FM2' },
        { id: 2, name: 'FM3' },
        { id: 3, name: 'AM1' },
        { id: 4, name: 'AM2' }
    ];

    bands.forEach(band => {
        const section = document.createElement('div');
        section.className = 'band-section';
        
        const bandPresets = currentData.presets.filter(p => p.band === band.id);
        const count = bandPresets.length;
        
        section.innerHTML = `
            <div class="band-header">${band.name} <span>${count}</span></div>
            <div class="preset-column" id="grid-band-${band.id}"></div>
        `;
        elements.presetGrid.appendChild(section);
        
        const bandGrid = section.querySelector(`#grid-band-${band.id}`);
        
        // Show 18 slots per band
        for (let i = 1; i <= 18; i++) {
            const preset = bandPresets.find(p => p.preset === i);
            if (preset) {
                const indexInMaster = currentData.presets.indexOf(preset);
                bandGrid.appendChild(createCard(preset, indexInMaster));
            } else {
                bandGrid.appendChild(createEmptyCard(band.id, i));
            }
        }
    });
    
    lucide.createIcons();
}

function createCard(p, index) {
    const div = document.createElement('div');
    const isAM = p.band >= 3;
    div.className = `preset-card band-${p.band} ${isAM ? 'is-am' : 'is-fm'}`;
    
    // Frequency logic: AM (kHz) vs FM (MHz)
    const freqDisplay = isAM ? p.frequency : (p.frequency / 1000).toFixed(1);
    const unit = isAM ? 'kHz' : 'MHz';
    
    div.innerHTML = `
        <span class="number">P-${p.preset}</span>
        <div class="name">${p.custom_name || translations[currentLang].noName}</div>
        <div class="frequency">${freqDisplay} <span>${unit}</span></div>
        <div class="card-actions">
            <button class="btn edit-btn" onclick="openEdit(${index})">
                <i data-lucide="edit-3"></i>
            </button>
            <button class="btn btn-danger delete-btn" onclick="deletePreset(${index})">
                <i data-lucide="trash-2"></i>
            </button>
        </div>
    `;
    return div;
}

function createEmptyCard(bandId, presetNum) {
    const div = document.createElement('div');
    const isAM = bandId >= 3;
    div.className = `preset-card empty band-${bandId} ${isAM ? 'is-am' : 'is-fm'}`;
    const unit = isAM ? 'kHz' : 'MHz';
    
    div.innerHTML = `
        <span class="number">P-${presetNum}</span>
        <div class="name">${translations[currentLang].empty}</div>
        <div class="frequency">--- <span>${unit}</span></div>
        <div class="card-actions" style="margin-top: auto;">
            <button class="btn" onclick="openAddAt(${bandId}, ${presetNum})">
                <i data-lucide="plus"></i> ${translations[currentLang].assign}
            </button>
        </div>
    `;
    return div;
}

window.openAddAt = (bandId, presetNum) => {
    document.getElementById('modalTitle').textContent = `${translations[currentLang].newPresetTitle} (${bandId >= 3 ? 'AM' : 'FM'}, Slot ${presetNum})`;
    document.getElementById('editIndex').value = '-1';
    elements.presetForm.reset();
    document.getElementById('formBand').value = bandId;
    document.getElementById('formPresetNum').value = presetNum;
    elements.modal.classList.add('active');
};

// --- CRUD Actions ---

window.deletePreset = (index) => {
    if (confirm(translations[currentLang].confirmDelete)) {
        currentData.presets.splice(index, 1);
        render();
    }
};

window.openEdit = (index) => {
    const p = currentData.presets[index];
    const isAM = p.band >= 3;
    
    document.getElementById('modalTitle').textContent = translations[currentLang].modalTitle;
    document.getElementById('editIndex').value = index;
    document.getElementById('formName').value = p.custom_name || '';
    
    // User-friendly frequency
    const displayFreq = isAM ? p.frequency : (p.frequency / 1000);
    document.getElementById('formFreq').value = displayFreq;
    
    document.getElementById('formBand').value = p.band;
    document.getElementById('formPresetNum').value = p.preset;
    
    elements.modal.classList.add('active');
};

elements.addBtn.onclick = () => {
    document.getElementById('modalTitle').textContent = translations[currentLang].newPresetTitle;
    document.getElementById('editIndex').value = '-1';
    elements.presetForm.reset();
    elements.modal.classList.add('active');
};

elements.cancelBtn.onclick = () => elements.modal.classList.remove('active');

elements.presetForm.onsubmit = (e) => {
    e.preventDefault();
    const index = parseInt(document.getElementById('editIndex').value);
    const bandId = parseInt(document.getElementById('formBand').value);
    const isAM = bandId >= 3;
    
    let rawFreq = document.getElementById('formFreq').value.replace(',', '.');
    const userFreq = parseFloat(rawFreq);
    
    if (isNaN(userFreq)) {
        alert(translations[currentLang].invalidFreq);
        return;
    }

    // Technical Validations
    if (!isAM && (userFreq < 87.5 || userFreq > 108.0)) {
        if (!confirm(`Frecuencia FM fuera de rango (87.5 - 108.0). ¿Continuar?`)) return;
    }
    if (isAM && (userFreq < 531 || userFreq > 1602)) {
        if (!confirm(`Frecuencia AM fuera de rango (531 - 1602). ¿Continuar?`)) return;
    }

    // Conversion to internal format (kHz)
    const internalFreq = isAM ? Math.round(userFreq) : Math.round(userFreq * 1000);

    const newPreset = {
        preset: parseInt(document.getElementById('formPresetNum').value),
        band: bandId,
        frequency: internalFreq,
        custom_name: document.getElementById('formName').value.toUpperCase().trim()
    };

    if (index === -1) {
        currentData.presets.push(newPreset);
    } else {
        currentData.presets[index] = newPreset;
    }
    
    // Auto-update timestamp
    const now = new Date();
    currentData.timestamp = now.toISOString().replace(/[-:T]/g, '').slice(0, 15);

    elements.modal.classList.remove('active');
    render();
};

elements.saveBtn.onclick = () => {
    const blob = new Blob([JSON.stringify(currentData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    
    const timestamp = new Date().toISOString().replace(/[-:T]/g, '').slice(0, 15);
    a.href = url;
    a.download = `favoritos_${timestamp}.fav`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    alert(translations[currentLang].successSave);
};
