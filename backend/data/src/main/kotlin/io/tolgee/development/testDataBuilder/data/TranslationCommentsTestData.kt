package io.tolgee.development.testDataBuilder.data

import io.tolgee.development.testDataBuilder.DataBuilders
import io.tolgee.development.testDataBuilder.TestDataBuilder
import io.tolgee.model.Language
import io.tolgee.model.Permission
import io.tolgee.model.Project
import io.tolgee.model.UserAccount
import io.tolgee.model.enums.TranslationState
import io.tolgee.model.key.Key
import io.tolgee.model.translation.Translation
import io.tolgee.model.translation.TranslationComment

class TranslationCommentsTestData {
  lateinit var firstComment: TranslationComment
  lateinit var secondComment: TranslationComment
  var project: Project
  lateinit var englishLanguage: Language
  var user: UserAccount
  var pepa: UserAccount
  lateinit var aKey: Key
  lateinit var bKey: Key
  lateinit var projectBuilder: DataBuilders.ProjectBuilder
  lateinit var translation: Translation

  val root: TestDataBuilder = TestDataBuilder().apply {
    user = addUserAccount {
      self {
        username = "franta"
      }
    }.self
    pepa = addUserAccount {
      self {
        username = "pepa"
      }
    }.self

    project = addProject {
      self {
        name = "Franta's project"
        userOwner = user
      }

      addPermission {
        self {
          project = this@addProject.self
          user = this@TranslationCommentsTestData.user
          type = Permission.ProjectPermissionType.MANAGE
        }
      }

      addPermission {
        self {
          project = this@addProject.self
          user = this@TranslationCommentsTestData.pepa
          type = Permission.ProjectPermissionType.EDIT
        }
      }

      englishLanguage = addLanguage {
        self {
          name = "English"
          tag = "en"
          originalName = "English"
        }
      }.self

      aKey = addKey {
        self.name = "A key"
        translation = addTranslation {
          self {
            key = this@addKey.self
            language = englishLanguage
            text = "Z translation"
            state = TranslationState.REVIEWED
          }
          firstComment = addComment {
            self {
              text = "First comment"
            }
          }.self

          secondComment = addComment {
            self {
              text = "Second comment"
            }
          }.self
        }.self
      }.self

      bKey = addKey {
        self.name = "B key"
      }.self

      projectBuilder = this
    }.self
  }

  fun addE2eTestData() {
    this.root.apply {
      val jindra = addUserAccount {
        self {
          username = "jindra"
        }
      }
      val vojta = addUserAccount {
        self {
          username = "vojta"
        }
      }
      projectBuilder.apply {
        addPermission {
          self {
            project = projectBuilder.self
            user = jindra.self
            type = Permission.ProjectPermissionType.TRANSLATE
          }
        }
        addPermission {
          self {
            project = projectBuilder.self
            user = vojta.self
            type = Permission.ProjectPermissionType.VIEW
          }
        }
        addKey {
          self.name = "C key"
          addTranslation {
            self {
              key = this@addKey.self
              language = englishLanguage
              text = "Bla translation"
              state = TranslationState.REVIEWED
            }
            firstComment = addComment {
              self {
                text = "First comment"
                author = jindra.self
              }
            }.self

            secondComment = addComment {
              self {
                text = "Second comment"
              }
            }.self
          }.self
        }.self

        addKey {
          self.name = "D key"
          addTranslation {
            self {
              key = this@addKey.self
              language = englishLanguage
              text = "Bla translation"
              state = TranslationState.REVIEWED
            }
            (1..50).forEach {
              addComment {
                self {
                  text = "comment $it"
                  author = jindra.self
                }
              }
            }
          }.self
        }.self
      }
    }
  }
}
