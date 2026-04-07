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
        tabFav: "Favoritos (.fav)",
        tabOptions: "Opciones (.ors)",
        orsTitle: "Generador de opciones",
        btnLoadOrs: "Cargar .ors",
        btnSaveOrs: "Guardar .ors",
        optCountry: "País",
        optAppLang: "Idioma app",
        optFont: "Fuente (tipo)",
        optIconPack: "Icon pack",
        optBgMode: "Modo fondo",
        optLastBand: "Última banda",
        optLastFreq: "Última frecuencia (kHz)",
        optSkin: "Skin",
        optLogosOnline: "Logos online",
        optAutoHide: "Auto ocultar controles",
        optLayoutV3: "Layout V3",
        optLayoutSimple: "Layout simple",
        optOnboardLangDone: "Onboarding idioma OK",
        optOnboardCountryDone: "Onboarding país OK",
        optOnboardLangCountryDone: "Onboarding idioma+país OK",
        optStationHistory: "Historial (pref_station_history)",
        optStationHistoryHelp: "Lista separada por comas en kHz (se puede dejar vacío).",
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
        newPresetTitle: "Nuevo Preset",
        orsInvalid: "Formato .ors no válido",
        orsSaved: "Archivo .ors generado y listo para descargar."
    },
    en: {
        pageTitle: "Favorites Management",
        emptyState: "Upload a file to start",
        dropTitle: "Drag your .fav file here",
        dropText: "or click to select",
        tabFav: "Favorites (.fav)",
        tabOptions: "Options (.ors)",
        orsTitle: "Options generator",
        btnLoadOrs: "Load .ors",
        btnSaveOrs: "Save .ors",
        optCountry: "Country",
        optAppLang: "App language",
        optFont: "Font (type)",
        optIconPack: "Icon pack",
        optBgMode: "Background mode",
        optLastBand: "Last band",
        optLastFreq: "Last frequency (kHz)",
        optSkin: "Skin",
        optLogosOnline: "Online logos",
        optAutoHide: "Auto-hide controls",
        optLayoutV3: "Layout V3",
        optLayoutSimple: "Simple layout",
        optOnboardLangDone: "Language onboarding OK",
        optOnboardCountryDone: "Country onboarding OK",
        optOnboardLangCountryDone: "Language+country onboarding OK",
        optStationHistory: "History (pref_station_history)",
        optStationHistoryHelp: "Comma-separated list in kHz (can be empty).",
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
        newPresetTitle: "New Preset",
        orsInvalid: "Invalid .ors format",
        orsSaved: ".ors file generated and ready to download."
    }
};

const elements = {
    tabFav: document.getElementById('tab-fav'),
    tabOrs: document.getElementById('tab-ors'),
    orsArea: document.getElementById('orsArea'),
    loadOrsBtn: document.getElementById('loadOrsBtn'),
    downloadOrsBtn: document.getElementById('downloadOrsBtn'),
    orsInput: document.getElementById('orsInput'),
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

// ORS controls
const orsControls = {
    country: document.getElementById('optCountry'),
    appLang: document.getElementById('optAppLang'),
    fontType: document.getElementById('optFontType'),
    iconPack: document.getElementById('optIconPack'),
    bgMode: document.getElementById('optBgMode'),
    lastBand: document.getElementById('optLastBand'),
    lastFreq: document.getElementById('optLastFreq'),
    skin: document.getElementById('optSkin'),
    logosOnline: document.getElementById('optLogosOnline'),
    autoHide: document.getElementById('optAutoHide'),
    layoutV3: document.getElementById('optLayoutV3'),
    layoutSimple: document.getElementById('optLayoutSimple'),
    onboardLangDone: document.getElementById('optOnboardLangDone'),
    onboardCountryDone: document.getElementById('optOnboardCountryDone'),
    onboardLangCountryDone: document.getElementById('optOnboardLangCountryDone'),
    stationHistory: document.getElementById('optStationHistory')
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

function setMode(mode) {
    const isFav = mode === 'fav';
    if (elements.tabFav && elements.tabOrs) {
        elements.tabFav.classList.toggle('active', isFav);
        elements.tabOrs.classList.toggle('active', !isFav);
    }
    if (elements.orsArea) elements.orsArea.style.display = isFav ? 'none' : 'block';
    elements.dropZone.style.display = isFav ? '' : 'none';
    // En modo opciones ocultamos el editor de presets si estaba abierto
    elements.editorArea.style.display = isFav ? elements.editorArea.style.display : 'none';
    if (!isFav) elements.emptyState.style.display = 'none';
}

elements.tabFav?.addEventListener('click', () => setMode('fav'));
elements.tabOrs?.addEventListener('click', () => setMode('ors'));

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

// ORS load/save
elements.loadOrsBtn?.addEventListener('click', () => elements.orsInput?.click());
elements.orsInput?.addEventListener('change', (e) => {
    const f = e.target.files && e.target.files[0];
    if (!f) return;
    const r = new FileReader();
    r.onload = () => {
        try {
            const obj = JSON.parse(String(r.result || ''));
            if (!obj || obj.schemaVersion !== 1 || !obj.RadioPresets || !obj.ThemePrefs) {
                throw new Error(translations[currentLang].orsInvalid);
            }
            fillOrsForm(obj);
        } catch (err) {
            alert(translations[currentLang].errorRead + (err?.message || err));
        }
    };
    r.readAsText(f);
});

elements.downloadOrsBtn?.addEventListener('click', () => {
    const ors = buildOrsJsonFromForm();
    const blob = new Blob([JSON.stringify(ors, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `opciones_${ors.timestamp}.ors`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    alert(translations[currentLang].orsSaved);
});

function nowTimestamp() {
    // yyyyMMdd_HHmmss
    const d = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    const yyyy = d.getFullYear();
    const MM = pad(d.getMonth() + 1);
    const dd = pad(d.getDate());
    const HH = pad(d.getHours());
    const mm = pad(d.getMinutes());
    const ss = pad(d.getSeconds());
    return `${yyyy}${MM}${dd}_${HH}${mm}${ss}`;
}

function buildOrsJsonFromForm() {
    const timestamp = nowTimestamp();
    const RadioPresets = {
        pref_font_type: parseInt(orsControls.fontType.value || '5', 10),
        pref_country_code: String(orsControls.country.value || 'ES'),
        pref_onboarding_lang_country_done: !!orsControls.onboardLangCountryDone.checked,
        pref_logos_online: !!orsControls.logosOnline.checked,
        pref_auto_hide_controls: !!orsControls.autoHide.checked,
        pref_last_band: parseInt(orsControls.lastBand.value || '0', 10),
        pref_bg_mode: parseInt(orsControls.bgMode.value || '2', 10),
        pref_station_history: String(orsControls.stationHistory.value || '').trim(),
        pref_last_freq: parseInt(orsControls.lastFreq.value || '0', 10),
        pref_icon_pack: parseInt(orsControls.iconPack.value || '0', 10),
        pref_layout_simple: !!orsControls.layoutSimple.checked,
        pref_onboarding_lang_done: !!orsControls.onboardLangDone.checked,
        pref_onboarding_country_done: !!orsControls.onboardCountryDone.checked,
        pref_layout_v3: !!orsControls.layoutV3.checked,
        app_language: String(orsControls.appLang.value || 'es')
    };

    const ThemePrefs = {
        selected_skin: String(orsControls.skin.value || 'CLASSIC'),
        prev_skin_before_night: String(orsControls.skin.value || 'CLASSIC')
    };

    return {
        schemaVersion: 1,
        timestamp,
        type: 'menu_only',
        RadioPresets,
        ThemePrefs
    };
}

function fillOrsForm(obj) {
    try {
        const rp = obj.RadioPresets || {};
        const tp = obj.ThemePrefs || {};
        orsControls.country.value = rp.pref_country_code || 'ES';
        orsControls.appLang.value = rp.app_language || 'es';
        orsControls.fontType.value = rp.pref_font_type ?? 5;
        orsControls.iconPack.value = rp.pref_icon_pack ?? 0;
        orsControls.bgMode.value = rp.pref_bg_mode ?? 0;
        orsControls.lastBand.value = rp.pref_last_band ?? 0;
        orsControls.lastFreq.value = rp.pref_last_freq ?? 0;
        orsControls.skin.value = tp.selected_skin || 'CLASSIC';
        orsControls.logosOnline.checked = !!rp.pref_logos_online;
        orsControls.autoHide.checked = !!rp.pref_auto_hide_controls;
        orsControls.layoutV3.checked = !!rp.pref_layout_v3;
        orsControls.layoutSimple.checked = !!rp.pref_layout_simple;
        orsControls.onboardLangDone.checked = !!rp.pref_onboarding_lang_done;
        orsControls.onboardCountryDone.checked = !!rp.pref_onboarding_country_done;
        orsControls.onboardLangCountryDone.checked = !!rp.pref_onboarding_lang_country_done;
        orsControls.stationHistory.value = rp.pref_station_history || '';
    } catch (e) {
        // noop
    }
}

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

// PWA: registro del service worker (HTTPS o localhost; necesario para “Instalar app” en Chrome/Android)
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('./sw.js', { scope: './' }).catch(() => {});
    });
}
