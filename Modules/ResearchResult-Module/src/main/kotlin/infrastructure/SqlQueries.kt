package infrastructure

internal object SqlQueries {
    const val CREATE_GLUCOSE_TABLE = """CREATE TABLE IF NOT EXISTS glucoconnectapi.glucose_measurements (
    id CHAR(36) PRIMARY KEY,
    glucose_concentration_encrypted TEXT NOT NULL,
    glucose_concentration_iv TEXT NOT NULL,
    unit VARCHAR(30) NOT NULL CHECK (unit IN ('MG_PER_DL', 'MMOL_PER_L')),
    timestamp TIMESTAMP NOT NULL,
    after_medication_encrypted TEXT,
    after_medication_iv TEXT,
    empty_stomach_encrypted TEXT,
    empty_stomach_iv TEXT,
    notes_encrypted TEXT,
    notes_iv TEXT,
    user_id CHAR(36),
    last_updated_on TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    is_synced BOOLEAN DEFAULT TRUE
);
        """

    const val CREATE_GLUCOSE_RESULT = """
        INSERT INTO glucoconnectapi.glucose_measurements (
            id, glucose_concentration_encrypted, glucose_concentration_iv,
            unit, timestamp,
            after_medication_encrypted, after_medication_iv,
            empty_stomach_encrypted, empty_stomach_iv,
            notes_encrypted, notes_iv,
            user_id
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
    """

    const val SYNC_GET = "SELECT id FROM glucoconnectapi.glucose_measurements WHERE id = ?;"

    const val SYNC_UPDATE = """
            UPDATE glucoconnectapi.glucose_measurements 
            SET glucose_concentration_encrypted = ?, glucose_concentration_iv = ?,
                unit = ?, timestamp = ?, 
                after_medication_encrypted = ?, after_medication_iv = ?,
                empty_stomach_encrypted = ?, empty_stomach_iv = ?,
                notes_encrypted = ?, notes_iv = ?,
                user_id = ?
            WHERE id = ?;
        """

    const val SYNC_INSERT = """
            INSERT INTO glucoconnectapi.glucose_measurements (
                id, glucose_concentration_encrypted, glucose_concentration_iv,
                unit, timestamp,
                after_medication_encrypted, after_medication_iv,
                empty_stomach_encrypted, empty_stomach_iv,
                notes_encrypted, notes_iv,
                user_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
        """

    const val GET_RESULTS_BY_USER_ID_BY_DATES = """
        SELECT id,
               glucose_concentration_encrypted, glucose_concentration_iv,
               unit,
               timestamp,
               user_id,
               last_updated_on,
               after_medication_encrypted, after_medication_iv,
               empty_stomach_encrypted, empty_stomach_iv,
               notes_encrypted, notes_iv
        FROM glucoconnectapi.glucose_measurements
        WHERE user_id = ? AND timestamp BETWEEN ? AND ?
    """

    const val GET_RESULT_BY_ID = """
        SELECT id,
           glucose_concentration_encrypted, glucose_concentration_iv,
           unit,
           timestamp,
           user_id,
           last_updated_on,
           after_medication_encrypted, after_medication_iv,
           empty_stomach_encrypted, empty_stomach_iv,
           notes_encrypted, notes_iv 
        FROM glucoconnectapi.glucose_measurements
        WHERE id = ?
    """

    const val GET_ALL_RESULTS = """
        SELECT id,
           glucose_concentration_encrypted, glucose_concentration_iv,
           unit,
           timestamp,
           user_id,
           last_updated_on,
           after_medication_encrypted, after_medication_iv,
           empty_stomach_encrypted, empty_stomach_iv,
           notes_encrypted, notes_iv 
        FROM glucoconnectapi.glucose_measurements WHERE (is_deleted IS FALSE OR NULL)  
    """
    const val GET_THREE_RESULTS_FOR_USER = """
        SELECT id,
           glucose_concentration_encrypted, glucose_concentration_iv,
           unit,
           timestamp,
           user_id,
           last_updated_on,
           after_medication_encrypted, after_medication_iv,
           empty_stomach_encrypted, empty_stomach_iv,
           notes_encrypted, notes_iv 
        FROM glucoconnectapi.glucose_measurements 
        WHERE user_id = ? AND (is_deleted IS FALSE OR NULL)  
        ORDER BY timestamp DESC
        LIMIT 3;
    """
    const val GET_RESULTS_FOR_USER = """
        SELECT id,
           glucose_concentration_encrypted, glucose_concentration_iv,
           unit,
           timestamp,
           user_id,
           last_updated_on,
           after_medication_encrypted, after_medication_iv,
           empty_stomach_encrypted, empty_stomach_iv,
           notes_encrypted, notes_iv 
        FROM glucoconnectapi.glucose_measurements 
        WHERE user_id = ? AND (is_deleted IS FALSE OR NULL)  
        ORDER BY timestamp DESC
        LIMIT 100;
    """

    const val UPDATE_RESULT = """
        UPDATE glucoconnectapi.glucose_measurements 
        SET 
            glucose_concentration_encrypted = ?, glucose_concentration_iv = ?,
            unit = ?, 
            timestamp = ? ,
            last_updated_on = ?,
            after_medication_encrypted = ?, after_medication_iv = ?,
            empty_stomach_encrypted = ?, empty_stomach_iv = ?,
            notes_encrypted = ?, notes_iv = ?
        WHERE id = ? AND (is_deleted IS FALSE OR NULL)  
    """

    const val HARD_DELETE = "DELETE FROM glucoconnectapi.glucose_measurements WHERE id = ?"

    const val SAFE_DELETE = """UPDATE glucoconnectapi.glucose_measurements
            SET is_deleted = ?
            WHERE id = ?"""
}
