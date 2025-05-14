use std::sync::Arc;

use crate::ports::primary::{UserService, AuthService};
use crate::ports::secondary::{UserRepository, AuthRepository};

use crate::adapters::primary::services::UserServiceImpl;
use crate::adapters::primary::services::auth_service_impl::AuthServiceImpl;