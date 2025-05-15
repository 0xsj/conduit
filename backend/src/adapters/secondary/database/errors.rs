// backend/src/adapters/secondary/database/errors.rs
pub fn map_sqlx_error(err: sqlx::Error) -> String {
    match err {
        sqlx::Error::Database(db_err) => {
            if let Some(code) = db_err.code() {
                if code == "23000" { // MySQL duplicate entry error
                    "A record with this data already exists".to_string()
                } else {
                    format!("Database error: {}", db_err)
                }
            } else {
                format!("Database error: {}", db_err)
            }
        },
        sqlx::Error::RowNotFound => "Record not found".to_string(),
        _ => format!("Database error: {}", err),
    }
}