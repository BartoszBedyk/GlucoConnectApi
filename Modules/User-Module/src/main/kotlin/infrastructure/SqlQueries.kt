package infrastructure

internal object SqlQueries {
    const val CREATE_USER_TABLE = """
            CREATE TABLE IF NOT EXISTS glucoconnectapi.users (
        id CHAR(36) PRIMARY KEY,
        first_name_encrypted TEXT,
        first_name_iv TEXT,
        last_name_encrypted TEXT,
        last_name_iv TEXT,
        email_encrypted TEXT NOT NULL,
        email_iv TEXT NOT NULL,
        email_hash VARCHAR(50) NULL,
        password VARCHAR(255) NOT NULL,
        user_type VARCHAR(50), 
        is_blocked BOOLEAN NOT NULL,
        pref_unit VARCHAR(50),
        diabetes_type_encrypted TEXT,
        diabetes_type_iv TEXT,
        last_updated_on TIMESTAMP,
        is_deleted BOOLEAN DEFAULT FALSE,
        is_synced BOOLEAN DEFAULT TRUE
        CHECK (user_type IN ('ADMIN', 'PATIENT', 'DOCTOR', 'OBSERVER'))
        CHECK (pref_unit IN ('MG_PER_DL', 'MMOL_PER_L'))) """

    const val CREATE_USER_STEP_ONE = """INSERT INTO glucoconnectapi.users
         (id, email_encrypted, email_iv, email_hash, password, is_blocked, last_updated_on)
         VALUES (?, ?, ?, ?, ?,?, ?) """

    const val CREATE_USER_STEP_TWO = """UPDATE glucoconnectapi.users
         SET first_name_encrypted = ?, first_name_iv = ?, last_name_encrypted = ?, last_name_iv = ?, pref_unit = ?,
         diabetes_type_encrypted = ?, diabetes_type_iv = ?, user_type = ?, last_updated_on = ?  WHERE id = ?;"""

    const val CREATE_USER_WITH_TYPE = """INSERT INTO glucoconnectapi.users 
        (id, email_encrypted, email_iv,email_hash, password, user_type, is_blocked, last_updated_on )
        VALUES (?, ?, ?, ?, ?, ?,?, ?) """

    const val GET_USER_BY_ID =
        """SELECT id, first_name_encrypted, first_name_iv, last_name_encrypted, last_name_iv, email_encrypted, email_iv,
           user_type, is_blocked, pref_unit, diabetes_type_encrypted, diabetes_type_iv
           FROM glucoconnectapi.users WHERE id = ?"""

    const val GET_ALL_USERS = "SELECT * FROM glucoconnectapi.users"

    const val GET_UNIT_BY_ID = "SELECT pref_unit FROM glucoconnectapi.users WHERE id = ?  "

    const val UPDATE_UNIT = "UPDATE glucoconnectapi.users SET pref_unit = ? WHERE id = ?;"

    const val UPDATE_USER_PROFILE_DATA = """UPDATE glucoconnectapi.users 
            SET first_name_encrypted = ?, first_name_iv = ?, last_name_encrypted = ?, last_name_iv = ?, pref_unit = ?,
            diabetes_type_encrypted = ?, diabetes_type_iv = ?, last_updated_on = ?
            WHERE id = ?;"""

    const val UPDATE_USER_TYPE = """UPDATE glucoconnectapi.users SET user_type = ?, last_updated_on = ? WHERE id = ?;"""

    const val UPDATE_USER_DIABETES_TYPE =
        """UPDATE glucoconnectapi.users SET diabetes_type_encrypted = ?, diabetes_type_iv = ?,
        last_updated_on =? WHERE id = ?;"""

    const val AUTHENTICATE =
        "SELECT * FROM glucoconnectapi.users WHERE email_hash = ? AND is_blocked = FALSE AND is_deleted = FALSE;"

    const val AUTHENTICATE_HASH = """SELECT password FROM glucoconnectapi.users WHERE email_hash = ?
        AND is_blocked = FALSE AND is_deleted = FALSE;"""

    const val OBSERVE_USER = """SELECT id, first_name_encrypted,first_name_iv, last_name_encrypted, last_name_iv,
        email_encrypted, email_iv, user_type, is_blocked, pref_unit, diabetes_type_encrypted, diabetes_type_iv
        FROM glucoconnectapi.users WHERE id LIKE ?"""

    const val BLOCK_USER = """UPDATE glucoconnectapi.users
        SET is_blocked = ?, last_updated_on = ?
        WHERE id = ?;"""

    const val UNBLOCK_USER = """UPDATE glucoconnectapi.users
        SET is_blocked = ?,  last_updated_on = ?
        WHERE id = ?;"""

    const val SOFT_DELETE = "UPDATE glucoconnectapi.users SET is_deleted = ?, last_updated_on = ? WHERE id = ?;"

    const val HARD_DELETE = "DELETE FROM glucoconnectapi.users WHERE id = ?"

    const val RESET_PASSWORD =
        """UPDATE glucoconnectapi.users SET password = ? WHERE id = ? AND is_blocked = FALSE AND is_deleted = FALSE"""
}
