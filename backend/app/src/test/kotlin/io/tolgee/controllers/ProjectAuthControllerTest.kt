package io.tolgee.controllers

import io.tolgee.dtos.response.ApiKeyDTO.ApiKeyDTO
import io.tolgee.fixtures.AuthRequestPerformer
import io.tolgee.fixtures.ProjectApiKeyAuthRequestPerformer
import io.tolgee.fixtures.ProjectAuthRequestPerformer
import io.tolgee.fixtures.ProjectJwtAuthRequestPerformer
import io.tolgee.model.Project
import io.tolgee.testing.AuthorizedControllerTest
import io.tolgee.testing.annotations.ProjectApiKeyAuthTestMethod
import io.tolgee.testing.annotations.ProjectJWTAuthTestMethod
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.ResultActions
import org.testng.annotations.BeforeMethod
import java.lang.reflect.Method

@SpringBootTest
@AutoConfigureMockMvc
abstract class ProjectAuthControllerTest(
  val projectUrlPrefix: String = "/api/project/"
) : AuthorizedControllerTest(), AuthRequestPerformer {

  // for api key auth methods
  val apiKey: ApiKeyDTO
    get() {
      val performer = this.projectAuthRequestPerformer
      return if (performer is ProjectApiKeyAuthRequestPerformer)
        performer.apiKey else
        throw Exception("Method not annotated with ApiKeyAccessTestMethod?")
    }

  val project: Project
    get() = this.projectAuthRequestPerformer.project

  var projectSupplier: (() -> Project)?
    get() = this.projectAuthRequestPerformer.projectSupplier
    set(value) {
      this.projectAuthRequestPerformer.projectSupplier = value
    }

  private var _projectAuthRequestPerformer: ProjectAuthRequestPerformer? = null

  private var projectAuthRequestPerformer: ProjectAuthRequestPerformer
    get() {
      return _projectAuthRequestPerformer
        ?: throw Exception("Method not annotated with ApiKeyAccessTestMethod nor ProjectJWTAuthTestMethod?")
    }
    set(value) {
      _projectAuthRequestPerformer = value
    }

  @BeforeMethod
  fun beforeEach(method: Method) {
    with(method.getAnnotation(ProjectApiKeyAuthTestMethod::class.java)) {
      if (this != null) {
        this@ProjectAuthControllerTest.projectAuthRequestPerformer =
          applicationContext!!.getBean(
            ProjectApiKeyAuthRequestPerformer::class.java,
            { userAccount },
            this.scopes,
            projectUrlPrefix
          )
      }
    }

    with(method.getAnnotation(ProjectJWTAuthTestMethod::class.java)) {
      if (this != null) {
        this@ProjectAuthControllerTest.projectAuthRequestPerformer =
          applicationContext!!.getBean(ProjectJwtAuthRequestPerformer::class.java, { userAccount }, projectUrlPrefix)
      }
    }
  }

  fun performProjectAuthPut(url: String, content: Any?): ResultActions {
    return projectAuthRequestPerformer.performProjectAuthPut(url, content)
  }

  fun performProjectAuthPost(url: String, content: Any?): ResultActions {
    return projectAuthRequestPerformer.performProjectAuthPost(url, content)
  }

  fun performProjectAuthGet(url: String): ResultActions {
    return projectAuthRequestPerformer.performProjectAuthGet(url)
  }

  fun performProjectAuthDelete(url: String, content: Any?): ResultActions {
    return projectAuthRequestPerformer.performProjectAuthDelete(url, content)
  }

  fun performProjectAuthMultipart(
    url: String,
    files: List<MockMultipartFile>,
    params: Map<String, Array<String>> = mapOf()
  ): ResultActions {
    return projectAuthRequestPerformer.performProjectAuthMultipart(url, files, params)
  }
}
