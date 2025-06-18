from passlib.context import CryptContext

# Password hashing context
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# Hash password before storing
def hash_password(password):
    return pwd_context.hash(password)