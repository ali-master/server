package io.tolgee.controllers.internal.e2e_data

import io.swagger.v3.oas.annotations.Hidden
import io.tolgee.development.DbPopulatorReal
import io.tolgee.dtos.request.OrganizationDto
import io.tolgee.exceptions.NotFoundException
import io.tolgee.security.InternalController
import io.tolgee.service.OrganizationRoleService
import io.tolgee.service.OrganizationService
import io.tolgee.service.UserAccountService
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"])
@Hidden
@RequestMapping(value = ["internal/e2e-data/organizations"])
@Transactional
@InternalController
class OrganizationE2eDataController(
  private val organizationService: OrganizationService,
  private val userAccountService: UserAccountService,
  private val dbPopulatorReal: DbPopulatorReal,
  private val organizationRoleService: OrganizationRoleService
) {
  @GetMapping(value = ["/create"])
  @Transactional
  fun createOrganizations() {
    data.forEach {
      val organization = organizationService.create(
        it.dto,
        this.dbPopulatorReal.createUserIfNotExists(it.owner.email, null, it.owner.name)
      )
    }

    data.forEach {
      val organization = organizationService.get(it.dto.slug!!)
      it.members.forEach { memberUserName ->
        userAccountService.getByUserName(memberUserName).orElseThrow { NotFoundException() }.let { user ->
          organizationRoleService.grantMemberRoleToUser(user, organization!!)
        }
      }

      it.otherOwners.forEach { memberUserName ->
        userAccountService.getByUserName(memberUserName).orElseThrow { NotFoundException() }.let { user ->
          organizationRoleService.grantOwnerRoleToUser(user, organization!!)
        }
      }
    }
  }

  @GetMapping(value = ["/clean"])
  @Transactional
  fun cleanupOrganizations() {
    organizationService.get("what-a-nice-organization")?.let {
      organizationService.delete(it.id)
    }
    data.forEach {
      organizationService.get(it.dto.slug!!)?.let { organization ->
        organizationService.delete(organization.id)
      }
    }
  }

  companion object {
    data class UserData(
      val email: String,
      val name: String = email
    )

    data class OrganizationDataItem(
      val dto: OrganizationDto,
      val owner: UserData,
      val otherOwners: MutableList<String> = mutableListOf(),
      val members: MutableList<String> = mutableListOf(),
    )

    val data = mutableListOf(
      OrganizationDataItem(
        dto = OrganizationDto(
          name = "Google",
          description = "An organization made by google company",
          slug = "google"
        ),
        owner = UserData("admin")
      ),
      OrganizationDataItem(
        dto = OrganizationDto(
          name = "Netsuite",
          description = "A system for everything",
          slug = "netsuite"
        ),
        owner = UserData("evan@netsuite.com", "Evan Goldberg")
      ),
      OrganizationDataItem(
        dto = OrganizationDto(
          name = "Microsoft",
          description = "A first software company ever or something like that.",
          slug = "microsoft"
        ),
        owner = UserData("gates@microsoft.com", "Bill Gates"),
        members = mutableListOf("admin")
      ),
      OrganizationDataItem(
        dto = OrganizationDto(
          name = "Tolgee",
          description = "This is us",
          slug = "tolgee"
        ),
        owner = UserData("admin"),
        otherOwners = mutableListOf("evan@netsuite.com"),
        members = mutableListOf("gates@microsoft.com", "cukrberg@facebook.com")
      ),
      OrganizationDataItem(
        dto = OrganizationDto(
          name = "Facebook",
          description = """
                            |This is an organization providing a great service to everyone for free. 
                            |They also develop amazing things like react and other open source stuff.
                            |However, they sell our data to companies.
                        """.trimMargin(),
          slug = "facebook"
        ),
        owner = UserData("cukrberg@facebook.com", "Mark Cukrberg"),
        otherOwners = mutableListOf("admin")
      ),
      OrganizationDataItem(
        dto = OrganizationDto(
          name = "Unknown company",
          description = "We are very unknown.",
          slug = "unknown-company"
        ),
        owner = UserData("admin")
      ),
      OrganizationDataItem(
        dto = OrganizationDto(
          name = "Techfides solutions s.r.o",
          description = "Lets develop the future",
          slug = "techfides-solutions"

        ),
        owner = UserData("admin")
      )
    )

    init {
      (1..20).forEach { number ->
        val email = "owner@zzzcool$number.com"
        data.add(
          OrganizationDataItem(
            dto = OrganizationDto(
              name = "ZZZ Cool company $number",
              description = "We are Z Cool company $number. What a nice day!",
              slug = "zzz-cool-company-$number"
            ),
            otherOwners = mutableListOf("admin"),
            owner = UserData(email),
          )
        )
        data.find { item -> item.dto.slug == "facebook" }!!.otherOwners.add(email)
      }
    }
  }
}
