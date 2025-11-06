package infrastructure

internal object SqlQueriesMedication {

    const val CREATE_MEDICATION_TABLE = """CREATE TABLE IF NOT EXISTS glucoconnectapi.medications (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    manufacturer VARCHAR(100),
    form VARCHAR(50), 
    strength VARCHAR(50)
);
"""

    const val CREATE_MEDICATION =
        """INSERT INTO glucoconnectapi.medications (id, name, description, manufacturer, form, strength)
            | VALUES (?,?,?,?,?,?); """

    const val CREATE_MEDICATIONS_SYNC =
        """INSERT INTO glucoconnectapi.medications (id, name, description, manufacturer, form, strength)
      
            | VALUES (?, ?, ?, ?, ?, ?); """

    const val GET_MEDICATION_BY_ID = """SELECT id, name, description, manufacturer, form, strength 
        |FROM glucoconnectapi.medications WHERE id = ?; """

    const val GET_ALL_MEDICATIONS = "SELECT * FROM glucoconnectapi.medications"

    const val HARD_DELETE_MEDICATION = "DELETE FROM glucoconnectapi.medications WHERE id = ?"

    const val GET_MEDICATIONS_SYNC = """SELECT m.* FROM glucoconnectapi.medications m 
        |JOIN glucoconnectapi.user_medications um ON m.id = um.medication_id 
        |WHERE um.is_synced = FALSE AND um.user_id = ?;"""
}
