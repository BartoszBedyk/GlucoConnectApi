package infrastructure

internal object SqlQueriesUserMedication {
    const val CREATE_USER_MEDICATION_TABLE = """CREATE TABLE IF NOT EXISTS glucoconnectapi.user_medications (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL REFERENCES glucoconnectapi.users(id) ON DELETE CASCADE,
    medication_id CHAR(36) NOT NULL REFERENCES glucoconnectapi.medications(id) ON DELETE CASCADE,
    dosage_encrypted TEXT,
    dosage_iv TEXT,
    frequency_encrypted TEXT,
    frequency_iv TEXT,
    start_date DATE,
    end_date DATE,
    notes_encrypted TEXT,
    notes_iv TEXT,
    last_updated_on TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    is_synced BOOLEAN DEFAULT TRUE
);
"""

    const val CREATE_USER_MEDICATION = """
        INSERT INTO glucoconnectapi.user_medications (id, user_id, medication_id, dosage_encrypted, dosage_iv, frequency_encrypted, frequency_iv , start_date, end_date, notes_encrypted, notes_iv)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?,? ,?, ?);
    """

    const val GET_USER_MEDICATION_BY_USER_ID = """
    SELECT 
        um.id,
        um.user_id, 
        um.medication_id, 
        um.dosage_encrypted, 
        um.dosage_iv, 
        um.frequency_encrypted, 
        um.frequency_iv, 
        um.start_date, 
        um.end_date, 
        um.notes_encrypted,
        um.notes_iv,
        m.name,
        m.description,
        m.manufacturer,
        m.form,
        m.strength
    FROM glucoconnectapi.user_medications um
    INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
    WHERE um.user_id = ? AND (um.start_date IS NULL OR um.start_date <= CURRENT_DATE) 
    AND (um.end_date IS NULL OR um.end_date >= CURRENT_DATE)
    AND um.is_deleted = false
    LIMIT 1;"""

    const val GET_USER_MEDICATION_BY_ID = """
        SELECT 
            um.id,
            um.user_id, 
            um.medication_id, 
            um.dosage_encrypted, 
            um.dosage_iv, 
            um.frequency_encrypted, 
            um.frequency_iv, 
            um.start_date, 
            um.end_date, 
            um.notes_encrypted,
            um.notes_iv,
            m.name,
            m.description,
            m.manufacturer,
            m.form,
            m.strength
        FROM glucoconnectapi.user_medications um
        INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
        WHERE um.id = ? ;
    """

    const val GET_USER_MEDICATION_BY_UID_AND_UMID = """
        SELECT 
               um.id,
            um.user_id, 
            um.medication_id, 
            um.dosage_encrypted, 
            um.dosage_iv, 
            um.frequency_encrypted, 
            um.frequency_iv, 
            um.start_date, 
            um.end_date, 
            um.notes_encrypted,
            um.notes_iv,
            m.name,
            m.description,
            m.manufacturer,
            m.form,
            m.strength
        FROM glucoconnectapi.user_medications um
        INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
        WHERE um.user_id = ? AND um.medication_id = ?;
    """

    const val GET_TODAY_MEDICATION_BY_USER_ID = """
        SELECT 
           um.id,
            um.user_id, 
            um.medication_id, 
            um.dosage_encrypted, 
            um.dosage_iv, 
            um.frequency_encrypted, 
            um.frequency_iv, 
            um.start_date, 
            um.end_date, 
            um.notes_encrypted,
            um.notes_iv,
            m.name,
            m.description,
            m.manufacturer,
            m.form,
            m.strength
        FROM glucoconnectapi.user_medications um
        INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
 WHERE um.user_id = ? 
AND (um.start_date IS NULL OR um.start_date <= CURRENT_DATE) 
AND (um.end_date IS NULL OR um.end_date >= CURRENT_DATE OR um.end_date IS NULL) AND um.is_deleted = false


    """

    const val GET_USER_MEDICATION_HISTORY = """
          SELECT *
FROM glucoconnectapi.user_medications um
        INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
        WHERE user_id = ?
ORDER BY end_date ASC NULLS LAST;
        """

    const val GET_USER_MEDICATION_ID = """
        SELECT 
            um.id
        FROM glucoconnectapi.user_medications um
        INNER JOIN glucoconnectapi.medications m ON um.medication_id = m.id
        WHERE um.user_id = ? AND um.medication_id = ?;
    """

}