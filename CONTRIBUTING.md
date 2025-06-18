
## 🚀 How to Contribute

### 1. Fork the Repository
Click the **"Fork"** button at the top of the [FaceID repository](https://github.com/HariNarayan04/FaceID) to create your own copy.

### 2. Clone Your Fork Locally
```bash
git clone https://github.com/your-username/FaceID.git
cd FaceID
```

### 3. Create a Feature Branch
Use a clear and descriptive name for your branch:
```bash
git checkout -b feature/your-feature-name
# Examples:
# git checkout -b feature/improve-face-detection
# git checkout -b fix/login-bug
# git checkout -b docs/update-readme
```

### 4. Make Your Changes
- Follow existing coding conventions and project structure
- Keep changes focused on a single feature or fix
- Comment your code where needed for clarity
- Respect the project's file structure:
  - Backend code in `FaceID_Backend/`
  - Mobile/Frontend code in `Frontend/`

### 5. Test Your Changes
- If your changes affect application logic, please test locally
- **Backend**: Run with `uvicorn` and test endpoints
- **Frontend**: Test using Android Studio on both emulator and physical devices where possible
- Use FastAPI's Swagger UI at `http://localhost:8000/` to test API endpoints

### 6. Commit and Push
Write clear, descriptive commit messages:
```bash
git add .
git commit -m "Add: Brief description of your change"
git push origin feature/your-feature-name
```

**Commit Message Examples:**
- `Add: Face detection accuracy improvements`
- `Fix: Login authentication issue`
- `Update: API documentation for new endpoints`
- `Refactor: Clean up ML model loading logic`

### 7. Open a Pull Request (PR)
- Go to your fork on GitHub and click **"Compare & pull request"**
- Write a clear title and description of your changes
- Link any relevant issues using `Fixes #issue-number` or `Closes #issue-number`
- Be responsive to feedback and make requested changes promptly

---

## 📌 Best Practices

### General Guidelines
- **Open an issue first** before starting work on large changes or new features
- Write clean, readable, and well-documented code
- Keep pull requests small and focused on a single improvement
- Test your changes thoroughly before submitting
- Be respectful and constructive in all interactions

### Code Quality
- Follow consistent naming conventions
- Add comments for complex logic
- Remove any debugging code or console logs before committing
- Ensure your code doesn't break existing functionality

---

## 🧪 Backend Development Tips

### Getting Started
- The backend uses **FastAPI** framework
- Store ML models and face matching logic under `FaceID_Backend/app/ml/`
- Follow Python PEP 8 style guidelines

### Testing & Development
- Use FastAPI's interactive API documentation at `http://localhost:8000/`
- Test all endpoints thoroughly before submitting changes
- Ensure proper error handling and validation

### API Guidelines
- Use appropriate HTTP status codes
- Provide clear error messages
- Follow RESTful conventions for endpoint naming

---

## 📱 Frontend Development Tips

### Technology Stack
- Built with **Android** using **Kotlin**
- Follows **Jetpack Compose** conventions
- Uses modern Android development best practices

### Development Guidelines
- Keep UI components modular and reusable
- Follow Material Design principles
- Implement proper error handling and user feedback
- Test on both emulator and physical devices when possible

### Code Organization
- Organize code into logical packages/modules
- Use meaningful variable and function names
- Implement proper state management

---

## 🐛 Reporting Issues

When reporting bugs or requesting features:

1. **Check existing issues** first to avoid duplicates
2. **Use descriptive titles** that summarize the problem
3. **Provide detailed descriptions** including:
   - Steps to reproduce (for bugs)
   - Expected vs actual behavior
   - System information (OS, device, etc.)
   - Screenshots or logs if applicable

### Issue Templates
- **Bug Report**: Describe the problem and how to reproduce it
- **Feature Request**: Explain the feature and why it would be useful
- **Documentation**: Suggest improvements to docs or guides

---

## 💬 Getting Help

- **GitHub Issues**: For bug reports and feature requests
- **Discussions**: For questions and general discussion about the project
- **Pull Request Reviews**: Don't hesitate to ask for feedback during development

---

## 🙏 Recognition

All contributors will be recognized in our project documentation. Thank you for helping make FaceID better!

---

## 📄 License

By contributing to FaceID, you agree that your contributions will be licensed under the same license as the project.

---

**Happy Contributing! 🚀**

*For any questions about contributing, feel free to open an issue or reach out to [@HariNarayan04](https://github.com/HariNarayan04).*