package com.example.mindlens.screens.splash

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.data.onboardingPagesList
import com.example.mindlens.ui.components.onboarding.OnboardingSinglePageContent
import com.example.mindlens.ui.components.onboarding.PageIndicator
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onOnboardingFinished: () -> Unit) {
    //for saving current page state
    val pagerState = rememberPagerState(pageCount = { onboardingPagesList.size })

    // coroutine scope
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            // top bar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MindLens",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp) // Padding for bottom area
            ) {
                // The Pager (swiping area)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    // calls the single page layout defined below
                    OnboardingSinglePageContent(pageData = onboardingPagesList[pageIndex])
                }

                // Bottom Section: Indicators and Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Custom Indicators
                    PageIndicator(
                        pageCount = onboardingPagesList.size,
                        currentPage = pagerState.currentPage
                    )

                    // Next Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage < onboardingPagesList.size - 1) {
                                    // Scroll to next page
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                } else {
                                    // On last page, finish onboarding
                                    onOnboardingFinished()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            // Change text on last page if desired, design shows "Next" for all
                            text = if (pagerState.currentPage == onboardingPagesList.size - 1) "Next" else "Next",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}