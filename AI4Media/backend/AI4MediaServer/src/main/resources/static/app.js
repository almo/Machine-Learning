function app() {
    return {
        lang: 'en', // Default language
        currentView: 'login', // Views: login, rss, compose, scheduled, stats
        isSidebarOpen: true,
        isGenerating: false,
        contentGenerated: false,
        expandedCategories: [],
        isSyncingNews: false,
        
        // Master-Detail State
        newsList: [],
        isLoadingNews: false,
        newsError: null,
        selectedArticle: null,
        currentPage: 1,
        itemsPerPage: 25,
        
        // Patterns for login background
        patterns: ['bg-pattern-1', 'bg-pattern-2', 'bg-pattern-3', 'bg-pattern-4', 'bg-pattern-5', 'bg-pattern-6'],
        currentPattern: '',

        // Auto-run on initialization
        init() {
            // Pick a random pattern
            this.currentPattern = this.patterns[Math.floor(Math.random() * this.patterns.length)];
        },

        // Translations dictionary
        i18n: {
            en: {
                login: { title: "AI4Media Platform", sub: "Sign in to access your workspace", google_btn: "Sign in with Google" },
                nav: { rss: "News", compose: "Write Content", scheduled: "Scheduled", stats: "Stats", settings: "Settings", logout: "Logout", switch_es: "Cambiar a Español", switch_en: "Switch to English" },
                rss: { 
                    title: "News", add: "Add Source", cat: "Categories", 
                    categories: { technology: "Technology", marketing: "Marketing", development: "Development" },
                    read: "Read original", curate_ai: "AI Generate", curate_manual: "Manual Post", time_ago: "ago", hours: "hours", 
                    show_read: "Show read", hide_read: "Hide read", mark_read: "Mark as read", mark_unread: "Mark as unread", read_badge: "Read",
                    no_sources: "No sources added.", edit_source: "Edit Source", source_name: "Source Name", source_url: "RSS URL", source_category: "Category", source_tags: "Tags (e.g. #AI #Tech)", cancel: "Cancel", save: "Save",
                    all_news: "All News", add_cat: "Add Category", cat_name: "Category Name", mark_all_read: "Mark all as read", filter_by: "Filtering by:",
                    showing: "Showing", to: "to", of: "of", entries: "entries", prev: "Prev", next: "Next", show: "Show:"
                },
                compose: { title: "AI Content Generator", sub: "Review and edit the generated content before scheduling.", url: "News URL (Source)", placeholder: "https://example.com/interesting-news", gen: "Generate Posts", creating: "Creating...", linkedin: "LinkedIn Post", twitter: "Twitter (X) Post", bump1: "Bump 1", bump1_sub: "(+3/4 hours)", bump2: "Bump 2", bump2_sub: "(+24 hours)", send_cfg: "Sending Configuration", send_sub: "Posts will be sent immediately or in the next available slot.", schedule_btn: "Schedule All", manual_title: "Create Manual Post", manual_sub: "Write and schedule your own custom content.", content_lbl: "Post Content", url_lbl: "Link URL (Optional)", tags_lbl: "Tags (e.g. #Tech)", networks_lbl: "Publish to", schedule_lbl: "Publishing Time", now: "Send Now", random: "Random Time", specific: "Specific Time", select_time: "Select Date & Time", post_btn: "Schedule Post" },
                sched: { title: "Publishing Queue", upcoming: "Upcoming Posts", empty: "No scheduled posts.", cal_title: "Visual Calendar", cal_via: "Via Iframe", cal_place: "Your Calendar Iframe goes here", cal_sub: "Replace the 'src' of the iframe with your real calendar URL.", scheduled_for: "Scheduled for:" },
                stats: { title: "Impact & Performance", sub: "Summary of your publications from the last 30 days.", posts: "Published Posts", interactions: "Total Interactions", clicks: "Link Clicks", activity: "Publishing Activity", day: "Day", days_ago: "14 days ago", today: "Today", vs_last: "vs last month" },
                alerts: { success: "Content successfully scheduled and sent to backend!", delete: "Are you sure you want to remove this post from the queue?", delete_source: "Are you sure you want to delete this source?", sync_success: "News synchronization started successfully.", sync_error: "Error starting news sync." },
                settings: { title: "Settings", sub: "Manage your preferences and integrations.", social_accounts: "Social Accounts", connected: "Connected", not_connected: "Not connected", connect: "Connect", reconnect: "Reconnect", error: "Error initiating OAuth login.", news_sync: "News Sync", sync_now: "Sync Now", syncing: "Syncing...", sources_manage: "Manage Sources & Synchronization", sources_manage_sub: "Add, edit, remove, and sync your RSS feeds.", actions: "Actions", expand_all: "Expand All", collapse_all: "Collapse All", unread: "Unread", sources_count: "Sources", last_sync: "Last Sync", success: "Success", sync_error: "Error", never: "Never" }
            },
            es: {
                login: { title: "Plataforma AI4Media", sub: "Inicia sesión para acceder a tu espacio", google_btn: "Iniciar sesión con Google" },
                nav: { rss: "Noticias", compose: "Redactar Contenido", scheduled: "Programados", stats: "Estadísticas", settings: "Configuración", logout: "Cerrar Sesión", switch_es: "Cambiar a Español", switch_en: "Switch to English" },
                rss: { 
                    title: "Noticias", add: "Añadir Fuente", cat: "Categorías", 
                    categories: { technology: "Tecnología", marketing: "Marketing", development: "Desarrollo" },
                    read: "Leer original", curate_ai: "Generar con IA", curate_manual: "Post Manual", time_ago: "Hace", hours: "horas", 
                    show_read: "Mostrar leídos", hide_read: "Ocultar leídos", mark_read: "Marcar como leído", mark_unread: "Marcar como no leído", read_badge: "Leído",
                    no_sources: "No hay fuentes.", edit_source: "Editar Fuente", source_name: "Nombre de la Fuente", source_url: "URL del RSS", source_category: "Categoría", source_tags: "Etiquetas (ej. #IA #Tech)", cancel: "Cancelar", save: "Guardar",
                    all_news: "Todas las Noticias", add_cat: "Añadir Categoría", cat_name: "Nombre de la Categoría", mark_all_read: "Marcar todo como leído", filter_by: "Filtrando por:",
                    showing: "Mostrando", to: "a", of: "de", entries: "entradas", prev: "Ant", next: "Sig", show: "Mostrar:"
                },
                compose: { title: "Generador de Contenido IA", sub: "Revisa y edita el contenido generado antes de programarlo.", url: "URL de la Noticia (Fuente)", placeholder: "https://ejemplo.com/noticia-interesante", gen: "Generar Posts", creating: "Creando...", linkedin: "Post LinkedIn", twitter: "Post Twitter (X)", bump1: "Bump 1", bump1_sub: "(+3/4 hours)", bump2: "Bump 2", bump2_sub: "(+24 hours)", send_cfg: "Configuración de Envío", send_sub: "Los posts se enviarán inmediatamente o en el próximo bloque disponible.", schedule_btn: "Programar Todo", manual_title: "Crear Post Manual", manual_sub: "Escribe y programa tu propio contenido personalizado.", content_lbl: "Contenido del Post", url_lbl: "URL del Enlace (Opcional)", tags_lbl: "Etiquetas (ej. #Tech)", networks_lbl: "Publicar en", schedule_lbl: "Momento de Publicación", now: "Enviar Ahora", random: "Tiempo Aleatorio", specific: "Hora Específica", select_time: "Seleccionar Fecha y Hora", post_btn: "Programar Post" },
                sched: { title: "Cola de Publicación", upcoming: "Próximos Envíos", empty: "No hay posts programados.", cal_title: "Calendario Visual", cal_via: "Vía Iframe", cal_place: "Aquí irá tu Iframe del Calendario", cal_sub: "Reemplaza el 'src' del iframe con la URL de tu backend.", scheduled_for: "Programado para:" },
                stats: { title: "Impacto y Rendimiento", sub: "Resumen de tus publicaciones de los últimos 30 días.", posts: "Posts Publicados", interactions: "Interacciones Totales", clicks: "Clics en Enlaces", activity: "Actividad de Publicación", day: "Día", days_ago: "Hace 14 días", today: "Hoy", vs_last: "vs mes anterior" },
                alerts: { success: "¡Contenido programado con éxito y enviado al backend!", delete: "¿Estás seguro de que deseas eliminar este post de la cola?", delete_source: "¿Estás seguro de que deseas eliminar esta fuente?", sync_success: "Sincronización de noticias iniciada con éxito.", sync_error: "Error al iniciar la sincronización de noticias." },
                settings: { title: "Configuración", sub: "Gestiona tus preferencias e integraciones.", social_accounts: "Cuentas Sociales", connected: "Conectado", not_connected: "No conectado", connect: "Conectar", reconnect: "Reconectar", error: "Error al iniciar sesión OAuth.", news_sync: "Sincronizar Noticias", sync_now: "Sincronizar Ahora", syncing: "Sincronizando...", sources_manage: "Gestionar Fuentes y Sincronización", sources_manage_sub: "Añade, edita, elimina y sincroniza tus feeds RSS.", actions: "Acciones", expand_all: "Expandir Todo", collapse_all: "Contraer Todo", unread: "No leídos", sources_count: "Fuentes", last_sync: "Última Sincronización", success: "Éxito", sync_error: "Error", never: "Nunca" }
            }
        },

        // Helper to get translation
        t(key) {
            return key.split('.').reduce((o, i) => (o ? o[i] : null), this.i18n[this.lang]) || key;
        },

        // Helper to capitalize strings
        capitalize(str) {
            return str.charAt(0).toUpperCase() + str.slice(1);
        },

        // Toggle language
        toggleLang() {
            this.lang = this.lang === 'en' ? 'es' : 'en';
        },
        
        // Helper to format dates
        formatDate(timestamp) {
            if (!timestamp) return this.t('settings.never');
            try {
                let date;
                if (typeof timestamp === 'object') {
                    if (timestamp.seconds !== undefined) {
                        date = new Date(timestamp.seconds * 1000);
                    } else if (timestamp.epochSecond !== undefined) {
                        date = new Date(timestamp.epochSecond * 1000);
                    } else {
                        date = new Date(timestamp);
                    }
                } else if (typeof timestamp === 'number') {
                    // Dynamically detect between seconds or milliseconds format
                    date = new Date(timestamp > 10000000000 ? timestamp : timestamp * 1000);
                } else {
                    date = new Date(timestamp);
                }
                if (isNaN(date.getTime())) return this.t('settings.never');
                return date.toLocaleString(this.lang === 'en' ? 'en-US' : 'es-ES', {
                    timeZone: 'Europe/Zurich',
                    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false
                });
            } catch (e) {
                return this.t('settings.never');
            }
        },

        // API Helper for Backend Communication (Ktor)
        async apiCall(endpoint, method = 'GET', body = null) {
            try {
                const token = window.getFirebaseAuthToken ? await window.getFirebaseAuthToken() : null;
                const headers = { 'Content-Type': 'application/json' };
                if (token) headers['Authorization'] = `Bearer ${token}`;

                const options = { method, headers };
                if (body) options.body = JSON.stringify(body);

                // Use relative path to avoid CORS issues
                const API_BASE = ''; 
                const response = await fetch(`${API_BASE}${endpoint}`, options);

                if (!response.ok) throw new Error(`API Error: ${response.status}`);
                // Si es un DELETE que no devuelve body, evitamos error al hacer json()
                if (method === 'DELETE') return true; 
                return await response.json();
            } catch (error) {
                console.error('Backend integration error:', error);
                throw error;
            }
        },

        // Show/Hide read articles flag
        showRead: true,

        // Selected Filter State (type can be 'all', 'category', or 'source')
        selectedFilter: { type: 'all', value: null },

        // Helper to change filter and reset page
        setFilter(type, value) {
            this.selectedFilter = { type, value };
            this.currentPage = 1;
        },

        // Get dynamically filtered news
        get filteredNews() {
            let filtered = this.newsList;
            
            if (this.selectedFilter.type === 'category') {
                const categorySourceIds = this.sources.filter(s => s.category === this.selectedFilter.value).map(s => String(s.id));
                filtered = filtered.filter(n => categorySourceIds.includes(String(n.sourceId)));
            } else if (this.selectedFilter.type === 'source') {
                filtered = filtered.filter(n => String(n.sourceId) === String(this.selectedFilter.value));
            }
            
            return filtered.filter(n => this.showRead || !n.read);
        },

        // Get paginated news
        get paginatedNews() {
            const start = (this.currentPage - 1) * this.itemsPerPage;
            return this.filteredNews.slice(start, start + parseInt(this.itemsPerPage));
        },
        get totalPages() {
            return Math.max(1, Math.ceil(this.filteredNews.length / this.itemsPerPage));
        },

        // Get title for current filter
        get currentFilterTitle() {
            if (this.selectedFilter.type === 'all') return this.t('rss.all_news');
            if (this.selectedFilter.type === 'category') return this.t('rss.categories.' + this.selectedFilter.value) || this.capitalize(this.selectedFilter.value);
            if (this.selectedFilter.type === 'source') return this.sources.find(s => s.id === this.selectedFilter.value)?.name || '';
            return '';
        },

        // Toggle read status for single article
        toggleRead(id) {
            const news = this.newsList.find(n => n.id === id);
            if (news) news.read = !news.read;
        },

        // Mark all as read for category or source
        markAllAsRead(type, value) {
            let newsToMark = [];
            if (type === 'category') {
                const categorySourceIds = this.sources.filter(s => s.category === value).map(s => String(s.id));
                newsToMark = this.newsList.filter(n => categorySourceIds.includes(String(n.sourceId)));
            } else if (type === 'source') {
                newsToMark = this.newsList.filter(n => String(n.sourceId) === String(value));
            }
            newsToMark.forEach(n => n.read = true);
        },

        // Categories list
        categories: [],

        // Category Modal State
        isCategoryModalOpen: false,
        newCategoryName: '',

        saveCategory() {
            const name = this.newCategoryName.trim();
            if (name) {
                const slug = name.toLowerCase().replace(/[^a-z0-9]/g, '-');
                if (!this.categories.includes(slug)) {
                    this.categories.push(slug);
                    // Insert directly into i18n dictionary dynamically
                    this.i18n.en.rss.categories[slug] = name;
                    this.i18n.es.rss.categories[slug] = name;
                }
            }
            this.isCategoryModalOpen = false;
            this.newCategoryName = '';
        },

        // Sources data structure (Starts empty, will be loaded from backend)
        sources: [],

        // Load sources from Ktor Backend
        async loadSources() {
            try {
                // Llamada real al backend en Ktor
                const fetchedSources = await this.apiCall('/api/sources');
                this.sources = Array.isArray(fetchedSources) ? fetchedSources.map(s => {
                    const statusStr = s.syncStatus || s.syncstatus || s.SyncStatus || s.status;
                    const timeVal = s.lastSyncTime || s.lastsynctime || s.LastSyncTime || s.lastUpdateTime;
                    return {
                        ...s,
                        syncStatus: typeof statusStr === 'string' ? statusStr.trim().toUpperCase() : null,
                        lastSyncTime: timeVal || null
                    };
                }) : [];
                             
                // Register any unknown categories from the backend
                const newCats = new Set(this.categories);
                this.sources.forEach(s => {
                    if (s.category) {
                        newCats.add(s.category);
                        this.i18n.en.rss.categories[s.category] = this.capitalize(s.category);
                        this.i18n.es.rss.categories[s.category] = this.capitalize(s.category);
                    }
                });
                this.categories = Array.from(newCats);
            } catch (e) {
                console.warn("Backend no disponible. Cargando datos de prueba (Mock).");
                this.categories = ['technology', 'marketing', 'development'];
                this.sources = [
                    { id: '1', name: 'TechCrunch', category: 'technology', url: 'https://techcrunch.com/feed', tags: '#Tech #AI', syncStatus: 'SUCCESS', lastSyncTime: new Date().toISOString() },
                    { id: '2', name: 'Xataka', category: 'technology', url: 'https://xataka.com/feed', tags: '#Gadgets', syncStatus: 'ERROR', lastSyncTime: new Date(Date.now() - 86400000).toISOString() },
                    { id: '3', name: 'HackerNews', category: 'development', url: 'https://news.ycombinator.com/rss', tags: '#Programming', syncStatus: null, lastSyncTime: null }
                ];
            }
        },

        // Calculate total articles per category
        getArticlesCountByCategory(category) {
            const categorySourceIds = this.sources.filter(s => s.category === category).map(s => String(s.id));
            return this.newsList.filter(n => categorySourceIds.includes(String(n.sourceId))).length;
        },

        // Source Modal State
        isSourceModalOpen: false,
        editingSourceId: null,
        sourceForm: { name: '', url: '', category: 'technology', tags: '' },

        openSourceModal(source = null) {
            if (source) {
                this.editingSourceId = source.id;
                this.sourceForm = { ...source };
                if (this.sourceForm.tags === undefined) this.sourceForm.tags = '';
            } else {
                this.editingSourceId = null;
                this.sourceForm = { name: '', url: '', category: this.categories[0] || 'technology', tags: '' };
            }
            this.isSourceModalOpen = true;
        },

        async saveSource() {
            try {
                if (this.editingSourceId) {
                    // Backend Integration: PUT /api/sources/{id}
                    const updatedSource = await this.apiCall(`/api/sources/${this.editingSourceId}`, 'PUT', this.sourceForm);
                    
                    // Update local state AFTER successful backend response
                    const index = this.sources.findIndex(s => s.id === this.editingSourceId);
                    if(index !== -1) this.sources[index] = updatedSource;
                } else {
                    // Backend Integration: POST /api/sources
                    const newSource = await this.apiCall('/api/sources', 'POST', this.sourceForm);
                    
                    // Update local state AFTER successful backend response
                    this.sources.push(newSource);
                }
                this.isSourceModalOpen = false;
            } catch (error) {
                console.error("Fallo al guardar en el backend", error);
                alert("Error de conexión al guardar la fuente. Asegúrate de que Ktor está corriendo.");
            }
        },

        async deleteSource(id) {
            if(confirm(this.t('alerts.delete_source'))) {
                try {
                    // Backend Integration: DELETE /api/sources/{id}
                    await this.apiCall(`/api/sources/${id}`, 'DELETE');
                    
                    // Update local state AFTER successful backend response
                    this.sources = this.sources.filter(s => s.id !== id);
                    if (this.selectedFilter.type === 'source' && this.selectedFilter.value === id) {
                        this.setFilter('all', null);
                    }
                } catch (error) {
                    console.error("Fallo al borrar en el backend", error);
                    alert("Error de conexión al borrar la fuente.");
                }
            }
        },

        // Category Expansion
        isCategoryExpanded(category) {
            return this.expandedCategories.includes(category);
        },
        toggleCategory(category) {
            if (this.isCategoryExpanded(category)) {
                this.expandedCategories = this.expandedCategories.filter(c => c !== category);
            } else {
                this.expandedCategories.push(category);
            }
        },
        expandAllCategories() {
            this.expandedCategories = [...this.categories];
        },
        collapseAllCategories() {
            this.expandedCategories = [];
        },

        // Data Calculation
        getSourcesForCategory(category) {
            return this.sources.filter(s => s.category === category);
        },
        getUnreadCountForSource(sourceId) {
            return this.newsList.filter(n => String(n.sourceId) === String(sourceId) && !n.read).length;
        },
        getUnreadCountForCategory(category) {
            const categorySources = this.getSourcesForCategory(category);
            return categorySources.reduce((total, source) => {
                return total + this.getUnreadCountForSource(source.id);
            }, 0);
        },
        
        getSourceName(sourceId) {
            const source = this.sources.find(s => String(s.id) === String(sourceId));
            return source ? source.name : 'Unknown';
        },

        async loadNews() {
            this.isLoadingNews = true;
            this.newsError = null;
            try {
                const data = await this.apiCall('/api/news');
                this.newsList = Array.isArray(data) ? data : [];
                if (this.newsList.length > 0) this.selectedArticle = this.newsList[0];
            } catch (e) {
                console.error("Failed to load news", e);
                this.newsError = "Error loading news from server.";
                // Mock data fallback
                this.newsList = [
                    { id: '1', sourceId: '1', title: 'El nuevo modelo de IA reduce el consumo energético en un 40%', summary: 'Una startup emergente ha publicado un nuevo paper detallando cómo su arquitectura de red neuronal optimizada logra resultados estado-del-arte consumiendo una fracción de la energía.', url: 'https://techcrunch.com/ejemplo-ia-energia', imageUrl: 'https://storage.cloud.google.com/cathartic_computer_club/Images/Artificial%20Intelligence.jpg', read: false, publishedAt: Date.now() - 7200000 },
                    { id: '2', sourceId: '2', title: 'Ktor 3.0 lanzado oficialmente: Mejoras de rendimiento y plugins', summary: 'El framework de JetBrains para Kotlin recibe una actualización masiva centrada en la concurrencia y simplificación del ecosistema de plugins.', url: 'https://xataka.com/ejemplo-ktor-3', read: true, publishedAt: Date.now() - 14400000 },
                    { id: '3', sourceId: '3', title: 'Por qué AlpineJS es suficiente para el 80% de las aplicaciones web', summary: 'Un análisis profundo sobre el peso de los frameworks modernos y cómo herramientas mínimas están recuperando terreno en el desarrollo frontend.', url: 'https://news.ycombinator.com/ejemplo-alpine', read: false, publishedAt: Date.now() - 18000000 }
                ];
                if (this.newsList.length > 0) this.selectedArticle = this.newsList[0];
            } finally {
                this.isLoadingNews = false;
            }
        },

        // Editor/composer state
        composeMode: 'ai', // 'ai' or 'manual'
        composeData: {
            url: '',
            linkedin: '',
            twitter: '',
            bump1: '',
            bump2: ''
        },

        // Manual composition state
        manualPost: {
            content: '',
            url: '',
            tags: '',
            linkedin: true,
            twitter: true,
            scheduleType: 'now', // 'now', 'random', or 'specific'
            specificTime: ''
        },

        // List of scheduled posts
        scheduledList: [
            { id: 101, network: 'linkedin', type: 'Main Post', content: '¿Sabías que Ktor acaba de lanzar su versión 3.0? Las mejoras en concurrencia son impresionantes. Lo estamos usando en nuestro backend y la diferencia es notable. 🚀 #Kotlin #Ktor #Backend', scheduledTime: 'Hoy, 14:00' },
            { id: 102, network: 'twitter', type: 'Main Post', content: 'Ktor 3.0 ya está aquí 🔥. JetBrains ha hecho un trabajo excelente mejorando el sistema de plugins. ¿Alguien más actualizando sus servidores hoy? #Kotlin', scheduledTime: 'Hoy, 14:00' },
            { id: 103, network: 'twitter', type: 'Bump 1', content: 'Por cierto, aquí os dejo el enlace a las notas de lanzamiento completas por si os perdisteis algún detalle: [Enlace]', scheduledTime: 'Hoy, 17:30' }
        ],

        // OAuth settings state
        oauthStatus: {
            twitter: false,
            linkedin: false
        },

        async loadSettings() {
            try {
                // Fetching OAuth status from Data Store via Backend
                this.oauthStatus = await this.apiCall('/api/auth/status');
            } catch (e) {
                console.warn("Failed to load OAuth status, using mock data.");
                this.oauthStatus = { twitter: false, linkedin: false };
            }
        },

        async initOAuth(provider) {
            try {
                const response = await this.apiCall(`/api/auth/init-${provider}`);
                if (response && response.url) window.location.href = response.url;
            } catch (e) {
                console.error(`Failed to init OAuth for ${provider}`, e);
                alert(this.t('settings.error'));
            }
        },

        async syncNews() {
            this.isSyncingNews = true;
            try { // Ktor backend call
                await this.apiCall('/api/news/sync', 'POST');                        
            } catch (e) {
                console.error("Failed to trigger news sync", e);
                alert(this.t('alerts.sync_error'));
            } finally {
                // Reload sources to fetch updated statuses
                await this.loadSources();
                
                // Give feedback even if the call is async
                setTimeout(() => {
                    this.isSyncingNews = false;
                }, 2000);
            }
        },

        // Application functions
        logout() {
            // Sign out from Firebase (this will trigger 'auth-logout' automatically)
            if(window.firebaseSignOut) window.firebaseSignOut();
            
            // Reset state
            this.contentGenerated = false;
            this.composeData = { url: '', linkedin: '', twitter: '', bump1: '', bump2: '' };
        },

        curateWithAI(article) {
            this.composeMode = 'ai';
            this.composeData.url = article.url;
            this.contentGenerated = false; // Reset editor state
            this.currentView = 'compose';
            
            // Auto-mark as read when curated
            const news = this.newsList.find(n => n.id === article.id);
            if (news) news.read = true;
        },

        curateManual(article) {
            this.composeMode = 'manual';
            this.manualPost.content = `${article.title}\n\n${article.summary || ''}`;
            this.manualPost.url = article.url;
            this.manualPost.tags = Array.isArray(article.tags) ? article.tags.map(t => t.startsWith('#') ? t : `#${t}`).join(' ') : '';
            this.currentView = 'compose';
            
            // Auto-mark as read when curated
            const news = this.newsList.find(n => n.id === article.id);
            if (news) news.read = true;
        },

        generateContent() {
            if (!this.composeData.url) return;
            
            this.isGenerating = true;
            this.contentGenerated = false;
            
            // Simulate backend call (Ktor)
            setTimeout(() => {
                this.composeData.linkedin = `He estado leyendo este artículo muy interesante sobre ${this.composeData.url.split('/').pop().replace(/-/g, ' ')}. \n\nCreo que el impacto en nuestra industria va a ser significativo durante este año. ¿Qué opináis vosotros? \n\nDejo el enlace en los comentarios. 👇\n\n#Innovacion #Tecnologia #Tendencias`;
                
                this.composeData.twitter = `Nuevo artículo interesante detectado 🚨\n\nExplican a fondo los cambios en la industria y me ha parecido muy acertado. Hilo corto 🧵👇\n\n${this.composeData.url}\n#TechNews`;
                
                this.composeData.bump1 = `(Continuación) Lo que más me ha llamado la atención es la parte donde hablan de optimización de recursos. Totalmente aplicable a nuestros proyectos actuales.`;
                
                this.composeData.bump2 = `En caso de que os lo perdierais ayer, sigo pensando que este artículo es una lectura obligatoria para esta semana. ¡Buen inicio de día! ☕️ \n${this.composeData.url}`;
                
                this.isGenerating = false;
                this.contentGenerated = true;
            }, 1500); // 1.5s loading simulation
        },

        schedulePosts() {
            // Add mock posts to the list
            const newId = Date.now();
            this.scheduledList.unshift({
                id: newId, network: 'linkedin', type: 'Main', content: this.composeData.linkedin.substring(0, 100) + '...', scheduledTime: this.lang === 'en' ? 'Tomorrow, 09:00' : 'Mañana, 09:00'
            });
            this.scheduledList.unshift({
                id: newId + 1, network: 'twitter', type: 'Main', content: this.composeData.twitter.substring(0, 100) + '...', scheduledTime: this.lang === 'en' ? 'Tomorrow, 09:00' : 'Mañana, 09:00'
            });
            
            alert(this.t('alerts.success'));
            
            // Clear and redirect
            this.composeData = { url: '', linkedin: '', twitter: '', bump1: '', bump2: '' };
            this.contentGenerated = false;
            this.currentView = 'scheduled';
        },

        async submitManualPost() {
            if (!this.manualPost.content) return;
            if (!this.manualPost.linkedin && !this.manualPost.twitter) {
                alert("Select at least one network.");
                return;
            }
            
            if (!this.manualPost.url || this.manualPost.url.trim() === '') {
                alert(this.lang === 'en' ? "URL cannot be empty." : "La URL no puede estar vacía.");
                return;
            }
            try {
                const parsedUrl = new URL(this.manualPost.url);
                if (parsedUrl.protocol !== 'http:' && parsedUrl.protocol !== 'https:') {
                    throw new Error("Invalid protocol");
                }
            } catch (e) {
                alert(this.lang === 'en' ? "Please enter a valid URL (e.g., https://example.com)." : "Por favor, introduce una URL válida (ej. https://ejemplo.com).");
                return;
            }
            
            let timeStr = 'NOW';
            if (this.manualPost.scheduleType === 'random') {
                timeStr = 'AUTOMATIC';
            } else if (this.manualPost.scheduleType === 'specific') {
                if (!this.manualPost.specificTime) {
                    alert(this.t('compose.select_time'));
                    return;
                }
                timeStr = this.manualPost.specificTime; // standard format returned is YYYY-MM-DDThh:mm
            }

            let networksArr = [];
            if (this.manualPost.linkedin) networksArr.push('linkedin');
            if (this.manualPost.twitter) networksArr.push('twitter');

            const payload = {
                userId: "dummy", // Validated / overridden on backend via principal user
                textContent: this.manualPost.content,
                urlContent: this.manualPost.url || "",
                tags: this.manualPost.tags || "",
                networks: networksArr.join('|'),
                scheduledTime: timeStr
            };

            try {
                await this.apiCall('/schedule', 'POST', payload);                        
                
                this.manualPost = { content: '', url: '', tags: '', linkedin: true, twitter: true, scheduleType: 'now', specificTime: '' };
                this.currentView = 'scheduled';
            } catch (error) {
                console.error("Failed to schedule post", error);                        
            }
        },

        deleteScheduled(id) {
            if(confirm(this.t('alerts.delete'))) {
                this.scheduledList = this.scheduledList.filter(post => post.id !== id);
            }
        }
    }
}