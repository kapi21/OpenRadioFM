import os
import xml.etree.ElementTree as ET

base_dir = r"c:\@MIS PROYECTOS\K706_RE\OpenRadioFM\app\src\main\res"
base_file = os.path.join(base_dir, "values", "strings.xml")

# The translations dictionary for the missing elements
# Languages: fr, de, pt, it, ru, ro, uk, sr, zh, ja
translations = {
    'status_bar_v2': {
        'fr': 'Barre d\'état (Layout 2)', 'de': 'Statusleiste (Layout 2)', 'pt': 'Barra de status (Layout 2)', 'it': 'Barra di stato (Layout 2)',
        'ru': 'Строка состояния (Layout 2)', 'ro': 'Bara de stare (Layout 2)', 'uk': 'Рядок стану (Layout 2)', 'sr': 'Статусна трака (Layout 2)',
        'zh': '状态栏 (布局 2)', 'ja': 'ステータスバー (レイアウト 2)'
    },
    'status_bar_enabled': {
        'fr': '✅ Barre d\'état activée', 'de': '✅ Statusleiste aktiviert', 'pt': '✅ Barra de status ativada', 'it': '✅ Barra di stato attivata',
        'ru': '✅ Строка состояния включена', 'ro': '✅ Bara de stare activată', 'uk': '✅ Рядок стану увімкнено', 'sr': '✅ Статусна трака омогућена',
        'zh': '✅ 状态栏已启用', 'ja': '✅ ステータスバー有効'
    },
    'status_bar_disabled': {
        'fr': '❌ Barre d\'état désactivée', 'de': '❌ Statusleiste deaktiviert', 'pt': '❌ Barra de status desativada', 'it': '❌ Barra di stato disattivata',
        'ru': '❌ Строка состояния отключена', 'ro': '❌ Bara de stare dezactivată', 'uk': '❌ Рядок стану вимкнено', 'sr': '❌ Статусна трака онемогућена',
        'zh': '❌ 状态栏已禁用', 'ja': '❌ ステータスバー無効'
    },
    'pty_none': {
        'fr': 'Aucun PTY', 'de': 'Kein PTY', 'pt': 'Sem PTY', 'it': 'Nessun PTY', 'ru': 'Без PTY', 'ro': 'Fără PTY', 'uk': 'Без PTY', 'sr': 'Без PTY', 'zh': '无 PTY', 'ja': 'PTYなし'
    },
    'pty_news': {
        'fr': 'Nouvelles', 'de': 'Nachrichten', 'pt': 'Notícias', 'it': 'Notizie', 'ru': 'Новости', 'ro': 'Știri', 'uk': 'Новини', 'sr': 'Вести', 'zh': '新闻', 'ja': 'ニュース'
    },
    'pty_current_affairs': {
        'fr': 'Actualités', 'de': 'Aktuelles', 'pt': 'Atualidades', 'it': 'Attualità', 'ru': 'Текущие события', 'ro': 'Actualități', 'uk': 'Поточні події', 'sr': 'Актуелности', 'zh': '时事', 'ja': '時事問題'
    },
    'pty_information': {
        'fr': 'Information', 'de': 'Information', 'pt': 'Informação', 'it': 'Informazioni', 'ru': 'Информация', 'ro': 'Informații', 'uk': 'Інформація', 'sr': 'Информације', 'zh': '信息', 'ja': '情報'
    },
    'pty_sport': {
        'fr': 'Sports', 'de': 'Sport', 'pt': 'Esportes', 'it': 'Sport', 'ru': 'Спорт', 'ro': 'Sport', 'uk': 'Спорт', 'sr': 'Спорт', 'zh': '体育', 'ja': 'スポーツ'
    },
    'pty_education': {
        'fr': 'Éducation', 'de': 'Bildung', 'pt': 'Educação', 'it': 'Educazione', 'ru': 'Образование', 'ro': 'Educație', 'uk': 'Освіта', 'sr': 'Образовање', 'zh': '教育', 'ja': '教育'
    },
    'pty_drama': {
        'fr': 'Drame', 'de': 'Drama', 'pt': 'Drama', 'it': 'Drammatica', 'ru': 'Драма', 'ro': 'Dramă', 'uk': 'Драма', 'sr': 'Драма', 'zh': '剧情', 'ja': 'ドラマ'
    },
    'pty_culture': {
        'fr': 'Culture', 'de': 'Kultur', 'pt': 'Cultura', 'it': 'Cultura', 'ru': 'Культура', 'ro': 'Cultură', 'uk': 'Культура', 'sr': 'Култура', 'zh': '文化', 'ja': '文化'
    },
    'pty_science': {
        'fr': 'Science', 'de': 'Wissenschaft', 'pt': 'Ciência', 'it': 'Scienza', 'ru': 'Наука', 'ro': 'Știință', 'uk': 'Наука', 'sr': 'Наука', 'zh': '科学', 'ja': '科学'
    },
    'pty_varied': {
        'fr': 'Varié', 'de': 'Verschiedenes', 'pt': 'Variedades', 'it': 'Vario', 'ru': 'Разное', 'ro': 'Diverse', 'uk': 'Різне', 'sr': 'Разно', 'zh': '杂项', 'ja': 'その他'
    },
    'pty_pop': {
        'fr': 'Musique Pop', 'de': 'Popmusik', 'pt': 'Música Pop', 'it': 'Musica Pop', 'ru': 'Поп-музыка', 'ro': 'Muzică Pop', 'uk': 'Поп-музика', 'sr': 'Поп музика', 'zh': '流行音乐', 'ja': 'ポップミュージック'
    },
    'pty_rock': {
        'fr': 'Musique Rock', 'de': 'Rockmusik', 'pt': 'Música Rock', 'it': 'Musica Rock', 'ru': 'Рок-музыка', 'ro': 'Muzică Rock', 'uk': 'Рок-музика', 'sr': 'Рок музика', 'zh': '摇滚音乐', 'ja': 'ロックミュージック'
    },
    'pty_easy_listening': {
        'fr': 'Écoute Facile', 'de': 'Leichtes Zuhören', 'pt': 'Música Ambiente', 'it': 'Ascolto Leggero', 'ru': 'Легкая музыка', 'ro': 'Ascultare Ușoară', 'uk': 'Легка музика', 'sr': 'Лагана музика', 'zh': '轻音乐', 'ja': 'ライトミュージック'
    },
    'pty_light_classical': {
        'fr': 'Classique Légère', 'de': 'Leichte Klassik', 'pt': 'Clássica Leve', 'it': 'Classica Leggera', 'ru': 'Легкая классика', 'ro': 'Clasică Ușoară', 'uk': 'Легка класика', 'sr': 'Лагана класика', 'zh': '轻古典', 'ja': 'ライトクラシック'
    },
    'pty_serious_classical': {
        'fr': 'Classique Sérieuse', 'de': 'Ernste Klassik', 'pt': 'Clássica Erudita', 'it': 'Classica Seria', 'ru': 'Серьезная классика', 'ro': 'Clasică Serioasă', 'uk': 'Класика', 'sr': 'Класика', 'zh': '古典音乐', 'ja': 'クラシック'
    },
    'pty_other_music': {
        'fr': 'Autre Musique', 'de': 'Andere Musik', 'pt': 'Outra Música', 'it': 'Altra Musica', 'ru': 'Другая музыка', 'ro': 'Altă Muzică', 'uk': 'Інша музика', 'sr': 'Остала музика', 'zh': '其他音乐', 'ja': 'その他の音楽'
    },
    'pty_weather': {
        'fr': 'Météo', 'de': 'Wetter', 'pt': 'Clima', 'it': 'Meteo', 'ru': 'Погода', 'ro': 'Meteo', 'uk': 'Погода', 'sr': 'Време', 'zh': '天气', 'ja': '天気'
    },
    'pty_finance': {
        'fr': 'Finances', 'de': 'Finanzen', 'pt': 'Finanças', 'it': 'Finanza', 'ru': 'Финансы', 'ro': 'Finanțe', 'uk': 'Фінанси', 'sr': 'Финансије', 'zh': '金融', 'ja': '金融'
    },
    'pty_children': {
        'fr': 'Enfants', 'de': 'Kinder', 'pt': 'Crianças', 'it': 'Bambini', 'ru': 'Детям', 'ro': 'Copii', 'uk': 'Дітям', 'sr': 'Деца', 'zh': '儿童', 'ja': '子供'
    },
    'pty_social': {
        'fr': 'Social', 'de': 'Soziales', 'pt': 'Social', 'it': 'Sociale', 'ru': 'Социальное', 'ro': 'Social', 'uk': 'Соціальне', 'sr': 'Друштвено', 'zh': '社会', 'ja': '社会'
    },
    'pty_religion': {
        'fr': 'Religion', 'de': 'Religion', 'pt': 'Religião', 'it': 'Religione', 'ru': 'Религия', 'ro': 'Religie', 'uk': 'Релігія', 'sr': 'Религија', 'zh': '宗教', 'ja': '宗教'
    },
    'pty_phone_in': {
        'fr': 'Appels entrants', 'de': 'Anrufe', 'pt': 'Chamadas', 'it': 'Chiamate', 'ru': 'Звонки в студию', 'ro': 'Apeluri', 'uk': 'Дзвінки в студію', 'sr': 'Позиви', 'zh': '热线', 'ja': '電話参加'
    },
    'pty_travel': {
        'fr': 'Voyages', 'de': 'Reisen', 'pt': 'Viagens', 'it': 'Viaggi', 'ru': 'Путешествия', 'ro': 'Călătorii', 'uk': 'Подорожі', 'sr': 'Путовања', 'zh': '旅游', 'ja': '旅行'
    },
    'pty_leisure': {
        'fr': 'Loisirs', 'de': 'Freizeit', 'pt': 'Lazer', 'it': 'Tempo Libero', 'ru': 'Досуг', 'ro': 'Timp Liber', 'uk': 'Дозвілля', 'sr': 'Слободно време', 'zh': '休闲', 'ja': 'レジャー'
    },
    'pty_jazz': {
        'fr': 'Jazz', 'de': 'Jazz', 'pt': 'Jazz', 'it': 'Jazz', 'ru': 'Джаз', 'ro': 'Jazz', 'uk': 'Джаз', 'sr': 'Џез', 'zh': '爵士', 'ja': 'ジャズ'
    },
    'pty_country': {
        'fr': 'Country', 'de': 'Country', 'pt': 'Country', 'it': 'Country', 'ru': 'Кантри', 'ro': 'Country', 'uk': 'Кантрі', 'sr': 'Кантри', 'zh': '乡村', 'ja': 'カントリー'
    },
    'pty_national': {
        'fr': 'National', 'de': 'National', 'pt': 'Nacional', 'it': 'Nazionale', 'ru': 'Национальная', 'ro': 'Național', 'uk': 'Національна', 'sr': 'Национално', 'zh': '国家', 'ja': '国内'
    },
    'pty_oldies': {
        'fr': 'Vieux succès', 'de': 'Oldies', 'pt': 'Clássicos', 'it': 'Vecchi successi', 'ru': 'Ретро', 'ro': 'Vechi', 'uk': 'Ретро', 'sr': 'Стари хитови', 'zh': '怀旧', 'ja': 'オールディーズ'
    },
    'pty_folk': {
        'fr': 'Folk', 'de': 'Folk', 'pt': 'Folk', 'it': 'Folk', 'ru': 'Фолк', 'ro': 'Folk', 'uk': 'Фолк', 'sr': 'Фолк', 'zh': '民谣', 'ja': 'フォーク'
    },
    'pty_documentary': {
        'fr': 'Documentaire', 'de': 'Dokumentation', 'pt': 'Documentário', 'it': 'Documentario', 'ru': 'Документальный', 'ro': 'Documentar', 'uk': 'Документальний', 'sr': 'Документарни', 'zh': '纪录片', 'ja': 'ドキュメンタリー'
    },
    'pty_alarm_test': {
        'fr': 'Test d\'alarme', 'de': 'Alarm-Test', 'pt': 'Teste de Alarme', 'it': 'Test Allarme', 'ru': 'Тест тревоги', 'ro': 'Test Alarmă', 'uk': 'Тест тривоги', 'sr': 'Тест аларма', 'zh': '报警测试', 'ja': 'アラームテスト'
    },
    'pty_alarm': {
        'fr': 'Alarme', 'de': 'Alarm', 'pt': 'Alarme', 'it': 'Allarme', 'ru': 'Тревога', 'ro': 'Alarmă', 'uk': 'Тривога', 'sr': 'Аларм', 'zh': '报警', 'ja': 'アラーム'
    },
    'logo_provider': {
        'fr': 'Fournisseur de logos', 'de': 'Logo-Anbieter', 'pt': 'Provedor de Logos', 'it': 'Provider di Loghi', 'ru': 'Провайдер логотипов', 'ro': 'Furnizor Logo', 'uk': 'Провайдер логотипів', 'sr': 'Провајдер логотипа', 'zh': 'Logo 提供商', 'ja': 'ロゴプロバイダー'
    },
    'provider_supabase': {
        'fr': 'Supabase (Communauté HD)', 'de': 'Supabase (HD-Community)', 'pt': 'Supabase (Comunidade HD)', 'it': 'Supabase (Community HD)', 'ru': 'Supabase (HD-сообщество)', 'ro': 'Supabase (Comunitate HD)', 'uk': 'Supabase (HD-спільнота)', 'sr': 'Supabase (ХД Заједница)', 'zh': 'Supabase (高清社区)', 'ja': 'Supabase（HD コミュニティ）'
    },
    'provider_radiobrowser': {
        'fr': 'Radio-Browser (Réserve)', 'de': 'Radio-Browser (Ersatz)', 'pt': 'Radio-Browser (Reserva)', 'it': 'Radio-Browser (Riserva)', 'ru': 'Radio-Browser (Резерв)', 'ro': 'Radio-Browser (Rezervă)', 'uk': 'Radio-Browser (Резерв)', 'sr': 'Radio-Browser (Резерва)', 'zh': 'Radio-Browser (备用)', 'ja': 'Radio-Browser（予備）'
    },
    'provider_both': {
        'fr': 'Combiné (Supabase > Web)', 'de': 'Kombiniert (Supabase > Web)', 'pt': 'Combinado (Supabase > Web)', 'it': 'Combinato (Supabase > Web)', 'ru': 'Комбинированный (Supabase > Web)', 'ro': 'Combinat (Supabase > Web)', 'uk': 'Комбінований (Supabase > Web)', 'sr': 'Комбиновано (Supabase > Web)', 'zh': '组合 (Supabase > 网络)', 'ja': '組み合わせ (Supabase > Web)'
    },
    'contrib_cloud': {
        'fr': 'Contribuer (Logos HD)', 'de': 'Mitwirken (HD-Logos)', 'pt': 'Contribuir (Logos HD)', 'it': 'Contribuisci (Loghi HD)', 'ru': 'Помочь сообществу (Логотипы HD)', 'ro': 'Contribuie (Logouri HD)', 'uk': 'Допомогти спільноті (Логотипи HD)', 'sr': 'Допринеси заједници (ХД)', 'zh': '贡献 (高清 Logo)', 'ja': '貢献する（HD ロゴ）'
    },
    'contrib_cloud_desc': {
        'fr': 'Envoyez auto-informations pour améliorer la DB globale.', 'de': 'Sende Infos, um globale DB zu verbessern.', 'pt': 'Envie informações para melhorar DB.', 'it': 'Invia info per migliorare il DB.', 'ru': 'Отправляйте инфо, чтобы улучшить БД.', 'ro': 'Trimite info pt a îmbunătăți DB-ul.', 'uk': 'Надсилайте інфо для покращення БД.', 'sr': 'Шаљите информације за базу.', 'zh': '自动发送信息以改进全球数据库。', 'ja': 'グローバルDBを改善するために情報を送信します。'
    },
    'edit_station_name': {
        'fr': 'Modifier le nom de la station', 'de': 'Sendernamen bearbeiten', 'pt': 'Editar nome da estação', 'it': 'Modifica nome stazione', 'ru': 'Изменить название радиостанции', 'ro': 'Editează numele stației', 'uk': 'Змінити назву радіостанції', 'sr': 'Уреди име станице', 'zh': '编辑电台名称', 'ja': '放送局名を編集'
    },
    'frequency_label': {
        'fr': 'Fréquence : %1$s MHz', 'de': 'Frequenz: %1$s MHz', 'pt': 'Frequência: %1$s MHz', 'it': 'Frequenza: %1$s MHz', 'ru': 'Частота: %1$s МГц', 'ro': 'Frecvență: %1$s MHz', 'uk': 'Частота: %1$s МГц', 'sr': 'Фреквенција: %1$s MHz', 'zh': '频率: %1$s MHz', 'ja': '周波数: %1$s MHz'
    },
    'searching_next': {
        'fr': 'Recherche du suivant...', 'de': 'Suche nächstes...', 'pt': 'Buscando o próximo...', 'it': 'Ricerca successiva...', 'ru': 'Поиск следующей...', 'ro': 'Căutare următoarea...', 'uk': 'Пошук наступної...', 'sr': 'Тражење следеће...', 'zh': '正在搜索下一个...', 'ja': '次を検索中...'
    },
    'scanning': {
        'fr': 'Numérisation...', 'de': 'Scannen...', 'pt': 'Escaneando...', 'it': 'Scansione...', 'ru': 'Сканирование...', 'ro': 'Scanare...', 'uk': 'Сканування...', 'sr': 'Скенирање...', 'zh': '扫描中...', 'ja': 'スキャン中...'
    },
    'scan_completed': {
        'fr': 'Numérisation terminée', 'de': 'Scan abgeschlossen', 'pt': 'Escaneamento concluído', 'it': 'Scansione completata', 'ru': 'Сканирование завершено', 'ro': 'Scanare completă', 'uk': 'Сканування завершено', 'sr': 'Скенирање завршено', 'zh': '扫描完成', 'ja': 'スキャン完了'
    },
    'identifying_rds': {
        'fr': 'Identification de la station (RDS)...', 'de': 'Sender identifizieren (RDS)...', 'pt': 'Identificando a estação (RDS)...', 'it': 'Identificazione stazione (RDS)...', 'ru': 'Определение радиостанции (RDS)...', 'ro': 'Identificare stație (RDS)...', 'uk': 'Визначення радіостанції (RDS)...', 'sr': 'Идентификација станице (RDS)...', 'zh': '正在识别电台 (RDS)...', 'ja': '放送局を識別中 (RDS)...'
    },
    'version': {
        'fr': 'Version %1$s', 'de': 'Version %1$s', 'pt': 'Versão %1$s', 'it': 'Versione %1$s', 'ru': 'Версия %1$s', 'ro': 'Versiunea %1$s', 'uk': 'Версія %1$s', 'sr': 'Верзија %1$s', 'zh': '版本 %1$s', 'ja': 'バージョン %1$s'
    },
    'radio_engine_label': {
        'fr': '⚙ MOTEUR RADIO : %1$s', 'de': '⚙ RADIO-MOTOR : %1$s', 'pt': '⚙ MOTOR RÁDIO : %1$s', 'it': '⚙ MOTORE RADIO : %1$s', 'ru': '⚙ МОТОР РАДИО : %1$s', 'ro': '⚙ MOTOR RADIO : %1$s', 'uk': '⚙ МОТОР РАДІО : %1$s', 'sr': '⚙ РАДИО МОТОР : %1$s', 'zh': '⚙ 广播引擎 : %1$s', 'ja': '⚙ ラジオエンジン : %1$s'
    },
    'freq_no_service': {
        'fr': 'FREQ: AUCUN_SERVICE\\nBAND: ---\\nFLAGS: ---', 'de': 'FREQ: KEIN_SERVICE\\nBAND: ---\\nFLAGS: ---', 'pt': 'FREQ: SEM_SERVICO\\nBAND: ---\\nFLAGS: ---', 'it': 'FREQ: NESSUN_SERVIZIO\\nBAND: ---\\nFLAGS: ---', 'ru': 'FREQ: НЕТ_СЕРВИСА\\nBAND: ---\\nFLAGS: ---', 'ro': 'FREQ: FĂRĂ_SERVICIU\\nBAND: ---\\nFLAGS: ---', 'uk': 'FREQ: НЕМАЄ_СЕРВІСУ\\nBAND: ---\\nFLAGS: ---', 'sr': 'FREQ: НЕМА_СЕРВИСА\\nBAND: ---\\nFLAGS: ---', 'zh': 'FREQ: 无服务\\nBAND: ---\\nFLAGS: ---', 'ja': 'FREQ: サービスなし\\nBAND: ---\\nFLAGS: ---'
    },
    'freq_error_read': {
        'fr': 'FREQ: ERREUR_LECTURE\\nBAND: ---\\n%1$s', 'de': 'FREQ: LESET_FEHLER\\nBAND: ---\\n%1$s', 'pt': 'FREQ: ERRO_LEITURA\\nBAND: ---\\n%1$s', 'it': 'FREQ: ERRORE_LETTURA\\nBAND: ---\\n%1$s', 'ru': 'FREQ: ОШИБКА_ЧТЕНИЯ\\nBAND: ---\\n%1$s', 'ro': 'FREQ: EROARE_CITIRE\\nBAND: ---\\n%1$s', 'uk': 'FREQ: ПОМИЛКА_ЧИТАННЯ\\nBAND: ---\\n%1$s', 'sr': 'FREQ: ГРЕШКА_ЧИТАЊА\\nBAND: ---\\n%1$s', 'zh': 'FREQ: 读取错误\\nBAND: ---\\n%1$s', 'ja': 'FREQ: 読み取りエラー\\nBAND: ---\\n%1$s'
    },
    'sqi_no_service': {
        'fr': 'SQI_INDEX.....: AUCUN_SERVICE', 'de': 'SQI_INDEX.....: KEIN_SERVICE', 'pt': 'SQI_INDEX.....: SEM_SERVICO', 'it': 'SQI_INDEX.....: NESSUN_SERVIZIO', 'ru': 'SQI_INDEX.....: НЕТ_СЕРВИСА', 'ro': 'SQI_INDEX.....: FĂRĂ_SERVICIU', 'uk': 'SQI_INDEX.....: НЕМАЄ_СЕРВІСУ', 'sr': 'SQI_INDEX.....: НЕМА_СЕРВИСА', 'zh': 'SQI_INDEX.....: 无服务', 'ja': 'SQI_INDEX.....: サービスなし'
    },
    'sqi_error_read': {
        'fr': 'SQI_INDEX.....: ERREUR_LECTURE', 'de': 'SQI_INDEX.....: LESET_FEHLER', 'pt': 'SQI_INDEX.....: ERRO_LEITURA', 'it': 'SQI_INDEX.....: ERRORE_LETTURA', 'ru': 'SQI_INDEX.....: ОШИБКА_ЧТЕНИЯ', 'ro': 'SQI_INDEX.....: EROARE_CITIRE', 'uk': 'SQI_INDEX.....: ПОМИЛКА_ЧИТАННЯ', 'sr': 'SQI_INDEX.....: ГРЕШКА_ЧИТАЊА', 'zh': 'SQI_INDEX.....: 读取错误', 'ja': 'SQI_INDEX.....: 読み取りエラー'
    },
    'af_list_scanning': {
        'fr': 'AF_LIST.......: [NUMÉRISATION]', 'de': 'AF_LIST.......: [SCANNEN]', 'pt': 'AF_LIST.......: [ESCANEANDO]', 'it': 'AF_LIST.......: [SCANSIONE]', 'ru': 'AF_LIST.......: [СКАНИРОВАНИЕ]', 'ro': 'AF_LIST.......: [SCANARE]', 'uk': 'AF_LIST.......: [СКАНУВАННЯ]', 'sr': 'AF_LIST.......: [СКЕНИРАЊЕ]', 'zh': 'AF_LIST.......: [扫描中]', 'ja': 'AF_LIST.......: [スキャン中]'
    },
    'chipset_mtk': {
        'fr': 'DETECTED_CHIPSET..: MTK8163_NATIVE', 'de': 'DETECTED_CHIPSET..: MTK8163_NATIVE', 'pt': 'DETECTED_CHIPSET..: MTK8163_NATIVE', 'it': 'DETECTED_CHIPSET..: MTK8163_NATIVE', 'ru': 'DETECTED_CHIPSET..: MTK8163_NATIVE', 'ro': 'DETECTED_CHIPSET..: MTK8163_NATIVE', 'uk': 'DETECTED_CHIPSET..: MTK8163_NATIVE', 'sr': 'DETECTED_CHIPSET..: MTK8163_NATIVE', 'zh': 'DETECTED_CHIPSET..: MTK8163_NATIVE', 'ja': 'DETECTED_CHIPSET..: MTK8163_NATIVE'
    },
    'favorites_reset_complete': {
        'fr': 'Favoris réinitialisés correctement', 'de': 'Favoriten erfolgreich zurückgesetzt', 'pt': 'Favoritos redefinidos com sucesso', 'it': 'Preferiti ripristinati correttamente', 'ru': 'Избранное успешно сброшено', 'ro': 'Favoritele au fost resetate cu succes', 'uk': 'Вибране успішно скинуто', 'sr': 'Омиљено успешно ресетовано', 'zh': '已成功重置收藏夹', 'ja': 'お気に入りが正しくリセットされました'
    },
    'history_cleared': {
        'fr': 'Historique effacé', 'de': 'Verlauf gelöscht', 'pt': 'Histórico apagado', 'it': 'Cronologia cancellata', 'ru': 'История очищена', 'ro': 'Istoric șters', 'uk': 'Історію очищено', 'sr': 'Историја обрисана', 'zh': '历史记录已清除', 'ja': '履歴がクリアされました'
    },
    'my_favorites': {
        'fr': 'Mes Favoris', 'de': 'Meine Favoriten', 'pt': 'Meus Favoritos', 'it': 'I Miei Preferiti', 'ru': 'Мое избранное', 'ro': 'Favoritele mele', 'uk': 'Моє вибране', 'sr': 'Моје омиљено', 'zh': '我的收藏', 'ja': '私のお気に入り'
    }
}

target_langs = {
    'fr': 'values-fr',
    'de': 'values-de',
    'pt': 'values-pt',
    'it': 'values-it',
    'ru': 'values-ru',
    'ro': 'values-ro',
    'uk': 'values-uk',
    'sr': 'values-sr',
    'zh': 'values-zh',
    'ja': 'values-ja'
}

for lang_code, folder in target_langs.items():
    lang_dir = os.path.join(base_dir, folder)
    if not os.path.exists(lang_dir):
        os.makedirs(lang_dir)
        
    xml_path = os.path.join(lang_dir, "strings.xml")
    
    # If the file doesn't exist, we duplicate the base xml entirely and translate keys
    if not os.path.exists(xml_path):
        import shutil
        shutil.copy(base_file, xml_path)
        
    # Read the target XML
    tree = ET.parse(xml_path)
    root = tree.getroot()
    existing_keys = [child.attrib.get('name') for child in root if 'name' in child.attrib]
    
    changed = False
    # If there are keys missing from base_root that exist in translations dict, we append them
    for key, trans_dict in translations.items():
        if key not in existing_keys:
            if lang_code in trans_dict:
                # Add it!
                new_elem = ET.Element('string', {'name': key})
                new_elem.text = trans_dict[lang_code]
                root.append(new_elem)
                changed = True
                
    if changed:
        # Save back
        tree.write(xml_path, encoding='utf-8', xml_declaration=True)
        print(f"Updated {folder}/strings.xml")
    else:
        print(f"No missing strings found in {folder}/strings.xml")
