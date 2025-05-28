-- ======================================================================
-- Torneo Assessment - Migración Inicial
-- Autor: Andrés Orduz Grimaldo
-- Versión: 1.0.0
-- Descripción: Crear esquema inicial para el sistema de torneos E-Sport
-- ======================================================================

-- Crear extensiones necesarias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ======================================================================
-- TABLA: categories
-- Descripción: Categorías de torneos (FPS, MOBA, etc.)
-- ======================================================================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(100) NOT NULL,
    alias VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Índices para categories
CREATE UNIQUE INDEX idx_category_code ON categories(code);
CREATE INDEX idx_category_active ON categories(active);
CREATE INDEX idx_category_deleted ON categories(deleted_at) WHERE deleted_at IS NULL;

-- ======================================================================
-- TABLA: games
-- Descripción: Tipos de juegos disponibles para torneos
-- ======================================================================
CREATE TABLE games (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    player_count INTEGER NOT NULL CHECK (player_count > 0),
    description VARCHAR(500),
    image_url VARCHAR(255),
    genre VARCHAR(50),
    platform VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Índices para games
CREATE UNIQUE INDEX idx_game_code ON games(code);
CREATE INDEX idx_game_active ON games(active);
CREATE INDEX idx_game_player_count ON games(player_count);
CREATE INDEX idx_game_genre ON games(genre);
CREATE INDEX idx_game_deleted ON games(deleted_at) WHERE deleted_at IS NULL;

-- ======================================================================
-- TABLA: users
-- Descripción: Usuarios del sistema (organizadores, participantes, espectadores)
-- ======================================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    profile_image_url VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('ADMIN', 'ORGANIZER', 'USER')),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone VARCHAR(20),
    birth_date DATE,
    country VARCHAR(3), -- ISO 3166-1 alpha-3
    timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Índices para users
CREATE UNIQUE INDEX idx_user_username ON users(username);
CREATE UNIQUE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_active ON users(active);
CREATE INDEX idx_user_deleted ON users(deleted_at) WHERE deleted_at IS NULL;

-- ======================================================================
-- TABLA: tournaments
-- Descripción: Torneos de videojuegos
-- ======================================================================
CREATE TABLE tournaments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    tournament_type VARCHAR(10) NOT NULL DEFAULT 'FREE' CHECK (tournament_type IN ('FREE', 'PAID')),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'IN_PROGRESS', 'FINISHED', 'CANCELLED')),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    game_id BIGINT NOT NULL REFERENCES games(id),
    organizer_id BIGINT NOT NULL REFERENCES users(id),
    max_participants INTEGER NOT NULL CHECK (max_participants > 0),
    current_participants INTEGER NOT NULL DEFAULT 0 CHECK (current_participants >= 0),
    entry_fee DECIMAL(10,2) DEFAULT 0.00 CHECK (entry_fee >= 0),
    prize_pool DECIMAL(10,2) DEFAULT 0.00 CHECK (prize_pool >= 0),
    commission_rate DECIMAL(5,4) DEFAULT 0.0500 CHECK (commission_rate >= 0 AND commission_rate <= 1),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    registration_start TIMESTAMP NOT NULL,
    registration_end TIMESTAMP NOT NULL,
    stream_url VARCHAR(255),
    stream_platform VARCHAR(30), -- Twitch, YouTube, Discord, etc.
    rules TEXT,
    banner_image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT chk_tournament_dates CHECK (end_date > start_date),
    CONSTRAINT chk_registration_dates CHECK (registration_end > registration_start),
    CONSTRAINT chk_registration_before_start CHECK (registration_end <= start_date),
    CONSTRAINT chk_paid_tournament_fee CHECK (
        (tournament_type = 'FREE' AND entry_fee = 0) OR 
        (tournament_type = 'PAID' AND entry_fee > 0)
    )
);

-- Índices para tournaments
CREATE INDEX idx_tournament_status ON tournaments(status);
CREATE INDEX idx_tournament_type ON tournaments(tournament_type);
CREATE INDEX idx_tournament_category ON tournaments(category_id);
CREATE INDEX idx_tournament_game ON tournaments(game_id);
CREATE INDEX idx_tournament_organizer ON tournaments(organizer_id);
CREATE INDEX idx_tournament_start_date ON tournaments(start_date);
CREATE INDEX idx_tournament_active ON tournaments(active);
CREATE INDEX idx_tournament_deleted ON tournaments(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_tournament_search ON tournaments USING gin(to_tsvector('spanish', name || ' ' || COALESCE(description, '')));

-- ======================================================================
-- TABLA: tournament_participants
-- Descripción: Participantes registrados en torneos
-- ======================================================================
CREATE TABLE tournament_participants (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED' CHECK (status IN ('REGISTERED', 'CONFIRMED', 'CANCELLED', 'DISQUALIFIED')),
    team_name VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_tournament_participant UNIQUE (tournament_id, user_id)
);

-- Índices para tournament_participants
CREATE INDEX idx_participant_tournament ON tournament_participants(tournament_id);
CREATE INDEX idx_participant_user ON tournament_participants(user_id);
CREATE INDEX idx_participant_status ON tournament_participants(status);

-- ======================================================================
-- TABLA: tickets
-- Descripción: Tickets para acceso a torneos de pago
-- ======================================================================
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    ticket_code VARCHAR(50) NOT NULL UNIQUE,
    qr_code TEXT, -- Base64 encoded QR image
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    commission DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (commission >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED' CHECK (status IN ('RESERVED', 'PAID', 'USED', 'EXPIRED', 'CANCELLED')),
    purchase_date TIMESTAMP,
    expiration_date TIMESTAMP NOT NULL,
    usage_date TIMESTAMP,
    payment_reference VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Índices para tickets
CREATE UNIQUE INDEX idx_ticket_code ON tickets(ticket_code);
CREATE INDEX idx_ticket_tournament ON tickets(tournament_id);
CREATE INDEX idx_ticket_user ON tickets(user_id);
CREATE INDEX idx_ticket_status ON tickets(status);
CREATE INDEX idx_ticket_expiration ON tickets(expiration_date);
CREATE INDEX idx_ticket_active ON tickets(active);

-- ======================================================================
-- TABLA: notifications
-- Descripción: Sistema de notificaciones
-- ======================================================================
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    tournament_id BIGINT REFERENCES tournaments(id),
    type VARCHAR(30) NOT NULL, -- EMAIL, WEBSOCKET, PUSH
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'READ')),
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para notifications
CREATE INDEX idx_notification_user ON notifications(user_id);
CREATE INDEX idx_notification_tournament ON notifications(tournament_id);
CREATE INDEX idx_notification_type ON notifications(type);
CREATE INDEX idx_notification_status ON notifications(status);
CREATE INDEX idx_notification_created ON notifications(created_at);

-- ======================================================================
-- TABLA: tournament_stages
-- Descripción: Etapas/fases de un torneo
-- ======================================================================
CREATE TABLE tournament_stages (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL, -- "Clasificatorias", "Semifinales", "Final", etc.
    stage_order INTEGER NOT NULL CHECK (stage_order > 0),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_stage_dates CHECK (end_date > start_date),
    CONSTRAINT uk_tournament_stage_order UNIQUE (tournament_id, stage_order)
);

-- Índices para tournament_stages
CREATE INDEX idx_stage_tournament ON tournament_stages(tournament_id);
CREATE INDEX idx_stage_order ON tournament_stages(stage_order);
CREATE INDEX idx_stage_status ON tournament_stages(status);

-- ======================================================================
-- TABLA: matches
-- Descripción: Partidas individuales dentro de un torneo
-- ======================================================================
CREATE TABLE matches (
    id BIGSERIAL PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id),
    stage_id BIGINT REFERENCES tournament_stages(id),
    match_number INTEGER NOT NULL,
    participant1_id BIGINT REFERENCES tournament_participants(id),
    participant2_id BIGINT REFERENCES tournament_participants(id),
    winner_id BIGINT REFERENCES tournament_participants(id),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    score_participant1 INTEGER DEFAULT 0,
    score_participant2 INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_different_participants CHECK (participant1_id != participant2_id),
    CONSTRAINT chk_winner_is_participant CHECK (
        winner_id IS NULL OR 
        winner_id = participant1_id OR 
        winner_id = participant2_id
    )
);

-- Índices para matches
CREATE INDEX idx_match_tournament ON matches(tournament_id);
CREATE INDEX idx_match_stage ON matches(stage_id);
CREATE INDEX idx_match_participant1 ON matches(participant1_id);
CREATE INDEX idx_match_participant2 ON matches(participant2_id);
CREATE INDEX idx_match_status ON matches(status);
CREATE INDEX idx_match_start_time ON matches(start_time);

-- ======================================================================
-- TABLA: audit_log
-- Descripción: Log de auditoría para cambios importantes
-- ======================================================================
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE
    old_values JSONB,
    new_values JSONB,
    user_id BIGINT REFERENCES users(id),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para audit_log
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_user ON audit_log(user_id);
CREATE INDEX idx_audit_action ON audit_log(action);
CREATE INDEX idx_audit_created ON audit_log(created_at);

-- ======================================================================
-- TRIGGERS para updated_at
-- ======================================================================

-- Función para actualizar updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Aplicar trigger a todas las tablas
CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_games_updated_at BEFORE UPDATE ON games FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tournaments_updated_at BEFORE UPDATE ON tournaments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tournament_participants_updated_at BEFORE UPDATE ON tournament_participants FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tickets_updated_at BEFORE UPDATE ON tickets FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tournament_stages_updated_at BEFORE UPDATE ON tournament_stages FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_matches_updated_at BEFORE UPDATE ON matches FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ======================================================================
-- DATOS INICIALES
-- ======================================================================

-- Insertar categorías predeterminadas
INSERT INTO categories (code, description, alias) VALUES
('FPS', 'First Person Shooter', 'Disparos'),
('MOBA', 'Multiplayer Online Battle Arena', 'MOBA'),
('BR', 'Battle Royale', 'Battle Royale'),
('RTS', 'Real Time Strategy', 'Estrategia'),
('RPG', 'Role Playing Game', 'RPG'),
('SPORTS', 'Sports & Racing', 'Deportes'),
('FIGHTING', 'Fighting Games', 'Peleas'),
('CARD', 'Card Games', 'Cartas');

-- Insertar juegos predeterminados
INSERT INTO games (code, full_name, player_count, description, genre, platform) VALUES
('CS2', 'Counter-Strike 2', 5, 'Juego táctico de disparos en primera persona', 'FPS', 'PC'),
('LOL', 'League of Legends', 5, 'MOBA competitivo con más de 160 campeones', 'MOBA', 'PC'),
('VALORANT', 'Valorant', 5, 'FPS táctico con habilidades especiales', 'FPS', 'PC'),
('DOTA2', 'Dota 2', 5, 'MOBA competitivo con mecánicas profundas', 'MOBA', 'PC'),
('FORTNITE', 'Fortnite', 4, 'Battle Royale con construcción', 'BR', 'Multi'),
('APEX', 'Apex Legends', 3, 'Battle Royale basado en equipos', 'BR', 'Multi'),
('OW2', 'Overwatch 2', 5, 'Shooter de héroes en equipos', 'FPS', 'Multi'),
('FIFA24', 'FIFA 24', 1, 'Simulador de fútbol', 'SPORTS', 'Multi'),
('SF6', 'Street Fighter 6', 1, 'Juego de peleas clásico', 'FIGHTING', 'Multi'),
('HS', 'Hearthstone', 1, 'Juego de cartas coleccionables', 'CARD', 'Multi');

-- Insertar usuario administrador por defecto
INSERT INTO users (username, email, password_hash, first_name, last_name, role, email_verified) VALUES
('admin', 'admin@torneoesport.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqyc4nfGhFMqg5n8bBTCzXu', 'Administrador', 'Sistema', 'ADMIN', true);

-- ======================================================================
-- COMENTARIOS EN TABLAS
-- ======================================================================

COMMENT ON TABLE categories IS 'Categorías de torneos para clasificación';
COMMENT ON TABLE games IS 'Tipos de juegos disponibles para torneos';
COMMENT ON TABLE users IS 'Usuarios del sistema con roles diferenciados';
COMMENT ON TABLE tournaments IS 'Torneos de videojuegos con toda su configuración';
COMMENT ON TABLE tournament_participants IS 'Participantes registrados en cada torneo';
COMMENT ON TABLE tickets IS 'Tickets de acceso para torneos de pago';
COMMENT ON TABLE notifications IS 'Sistema de notificaciones multi-canal';
COMMENT ON TABLE tournament_stages IS 'Etapas o fases de un torneo';
COMMENT ON TABLE matches IS 'Partidas individuales dentro de torneos';
COMMENT ON TABLE audit_log IS 'Log de auditoría para trazabilidad';

-- ======================================================================
-- FIN DE LA MIGRACIÓN
-- ====================================================================== 