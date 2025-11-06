package infrastructure

internal object SqlQueries {

    const val CREATE_HEARTBEAT_TABLE = """
            CREATE TABLE IF NOT EXISTS glucoconnectapi.heartbeat_measurements (
            id CHAR(36) PRIMARY KEY NOT NULL,
            user_id CHAR(36) NOT NULL,
            timestamp TIMESTAMP NOT NULL,
            systolic_pressure_encrypted TEXT NOT NULL,
            systolic_pressure_iv TEXT NOT NULL,
            diastolic_pressure_encrypted TEXT NOT NULL,
            diastolic_pressure_iv TEXT NOT NULL,
            pulse_encrypted TEXT NOT NULL,
            pulse_iv TEXT NOT NULL,
            note_encrypted TEXT NOT NULL,
            note_iv TEXT NOT NULL,
            last_updated_on TIMESTAMP,
            is_deleted BOOLEAN DEFAULT FALSE,
            is_synced BOOLEAN DEFAULT TRUE
            )
        """
    const val CREATE_HEARTBEAT_RESULT = """
            INSERT INTO glucoconnectapi.heartbeat_measurements (id, user_id, timestamp, systolic_pressure_encrypted, systolic_pressure_iv,
             diastolic_pressure_encrypted, diastolic_pressure_iv, pulse_encrypted, pulse_iv, note_encrypted, note_iv)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
        """

    const val GET_HEARTBEAT_BY_ID = """
            SELECT id, user_id, timestamp, systolic_pressure_encrypted, systolic_pressure_iv, diastolic_pressure_encrypted,
             diastolic_pressure_iv, pulse_encrypted, pulse_iv, note_encrypted, note_iv FROM glucoconnectapi.heartbeat_measurements 
            WHERE id = ?
        """

    const val GET_HEARTBEAT_BY_USER_ID =
        "SELECT * FROM glucoconnectapi.heartbeat_measurements  WHERE user_id = ? ORDER BY timestamp DESC LIMIT 100"
    const val GET_THREE_HEARTBEAT =
        "SELECT * FROM glucoconnectapi.heartbeat_measurements  WHERE user_id = ? ORDER BY timestamp DESC LIMIT 3"
    const val HARD_DELETE_HEARTBEAT_BY_ID = "DELETE FROM glucoconnectapi.heartbeat_measurements  WHERE id = ?"
    const val HARD_DELETE_HEARTBEATS_BY_USER_ID =
        "DELETE FROM glucoconnectapi.heartbeat_measurements  WHERE user_id = ?"


}